package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.*
import de.xai.handwriting_labeling_app_backend.component.BatchConfigHandler
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_FINISHED
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_SUCCESS
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.OTHERS_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.othersDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.roleExpert
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.roleUser
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import kotlin.jvm.optionals.getOrElse

@Service
class BatchService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val sampleRepository: SampleRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository,
    private val examplePairRepository: ExamplePairRepository,
    private val answerRepository: AnswerRepository,
    private val configHandler: BatchConfigHandler
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateBatch(username: String): GetBatchResponseBody {
        val config = configHandler.readBatchServiceConfig()

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")
        logger.info("Generating random batch for user $user")

        val samplesDirectory = when (config.samplesOrigin) {
            XAI_SENTENCE_DIRECTORY_NAME -> xaiSentencesDirectory
            OTHERS_DIRECTORY_NAME -> othersDirectory
            else -> throw IllegalArgumentException("Sample origin is not supported. No directory corresponds to ${config.samplesOrigin}")
        }

        val userRoles = user.roles.mapNotNull { it.name }
        val taskBatchBodyForExpert = if (roleExpert in userRoles) {
            // user is expert -> first try to generate a batch of samples where expert answer is missing
            findBatch(
                targetAnswerCount = config.targetAnswerCount,
                batchSize = config.batchSize,
                samplesDirectory = samplesDirectory,
                userId = user.id!!,
                userRole = roleExpert,
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList()
            )
        } else null
        // if no batch for expert answers was assembled, then create batch where user(=any) answer is missing
        val taskBatchBody = taskBatchBodyForExpert
            ?: findBatch(
                targetAnswerCount = config.targetAnswerCount,
                batchSize = config.batchSize,
                samplesDirectory = samplesDirectory,
                userId = user.id!!,
                userRole = roleUser,
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList()
            )


        if (taskBatchBody == null) {
            return GetBatchResponseBody(state = GET_BATCH_RESPONSE_STATE_FINISHED, null)
        }
        return GetBatchResponseBody(
            state = GET_BATCH_RESPONSE_STATE_SUCCESS,
            body = taskBatchBody
        )
    }

    /**
     *
     * ## Explanation:
     * We iterate combinations of question and reference sentence.
     * The order is determined by the priorities given in BatchServiceConfig.
     *
     * One after the other we try to assemble a batch of samples that refer to the given refSent and quest.
     *      Thereby we check:
     *      - is the question applicable to the reference sentence?
     *      - are there samples for which the user has not posted an answer yet?
     *      - do the samples not already have enough (targetAnswerCount) answers from users with the given role?
     *      If there are samples that comply, then a batch is assembled randomly from them.
     *      If not, then the loop continues with the next combination of quest and refSent
     * Return null if no batch was assembled for any of the combinations.
     *
     *
     *  ## Known issues/inconsistencies:
     * - More answers for sample and question then specified in BatchServiceConfig:
     *      If user A loads a batch and then user B load a batch, than both can have the same sample that is just
     *      missing one answer in their batch.
     *      Consequently, both user A and B will send a POST answer for this sample when they label the batch.
     *      THEN this sample will have more answers than specified in the config. This is because we only check
     *      the DB state and config when we create a batch. We do not check on POST answer.
     * */
    fun findBatch(
        targetAnswerCount: Int,
        batchSize: Int,
        samplesDirectory: File,
        userId: Long,
        userRole: String,
        possiblePrioritizedQuestions: MutableList<PrioritizedQuestion>,
        possiblePrioritizedSentences: MutableList<PrioritizedReferenceSentence>
    ): TaskBatchInfoBody? {
        // get questions and reference sentence that are stored in DB
        val priorityToQuestionPairs = possiblePrioritizedQuestions.map { prioritizedQuestion ->
            prioritizedQuestion to questionRepository.findById(prioritizedQuestion.questionId).getOrElse {
                throw IllegalStateException("Question ${prioritizedQuestion.questionId} does not exist.")
            }
        }
        val priorityToSentencePAirs = possiblePrioritizedSentences.map { prioritizedSentence ->
            prioritizedSentence to referenceSentenceRepository.findById(prioritizedSentence.referenceSentencesId).getOrElse {
                throw IllegalStateException("Sentence ${prioritizedSentence.referenceSentencesId} does not exist.")
            }
        }

        // shuffle before sort, to randomly pick between same priority
        for (prioToQuestion in priorityToQuestionPairs.shuffled().sortedBy { it.first.priority }) {
            for (prioToSentence in priorityToSentencePAirs.shuffled().sortedBy { it.first.priority }) {
                val question = prioToQuestion.second
                val sentence = prioToSentence.second

                if (question !in sentence.applicableQuestions!!) {
                    // question not applicable to sentence, continue with next sentence
                    continue
                }

                // all samples that correspond to the selected reference sentence
                val refSentSamples = sampleRepository.findAllInDirectoryRecursive(samplesDirectory).filter { sample ->
                    sentence.id == sample.referenceSentence?.id
                }
                // all samples that correspond to the selected sentence, that the user has not answered the question yet
                val refSentSamplesNotAnsweredByUser = refSentSamples.filter { sample ->
                    !sampleHasQuestionAnswerByUser(sample, userId, question.id!!)
                }
                if (refSentSamplesNotAnsweredByUser.isEmpty()) {
                    // the user answered all samples for this sentence and question
                    continue
                }

                val samples = refSentSamplesNotAnsweredByUser

                val samplesToAnswerCount = samples.map { sample ->
                    val answerToSampleAndQuestion =
                        answerRepository.findAllByQuestionIdAndSampleId(question.id!!, sample.id)
                    // only count answers where the answerer has the same role as the user who is currently requesting a new batch
                    val answerToSampleAndQuestionWithRole = answerToSampleAndQuestion.filter { answer ->
                        val rolesOfAnswerer = answer.user?.roles?.mapNotNull { it.name } ?: setOf()
                        userRole in rolesOfAnswerer
                    }
                    sample to answerToSampleAndQuestionWithRole.size
                }

                // start by collecting at least one answer per sample. If all samples have one answer, then collect answers
                // until every sample has 2 answers, .... until every sample has targetAnswerCount samples.
                // Then stop collecting answers for this combo of sentence and question
                for (answerCount in 1..targetAnswerCount) {
                    val samplesToMakeBatchFrom = samplesToAnswerCount.filter { sampleToCount ->
                        sampleToCount.second < answerCount
                    }.map { sampleToCount -> sampleToCount.first }

                    if (samplesToMakeBatchFrom.isNotEmpty()) {
                        val batchSamples = samplesToMakeBatchFrom.shuffled().take(batchSize)
                        val examplePair =
                            examplePairRepository.findByReferenceSentenceAndQuestion(sentence, question)
                        return TaskBatchInfoBody(
                            question = question,
                            referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(sentence),
                            examplePair = ExamplePairInfoBody.fromExamplePair(examplePair!!),
                            samples = batchSamples.map { SampleInfoBody.fromSample(it) })
                    }
                }
            }
        }
        return null
    }

    private fun sampleHasQuestionAnswerByUser(sample: Sample, userId: Long, questionId: Long): Boolean {
        val userAnswersForQuestion = answerRepository.findAllByUserIdAndQuestionId(userId, questionId)
        for (answer in userAnswersForQuestion) {
            if (answer.sampleId == sample.id) {
                return true
            }
        }
        return false
    }
}
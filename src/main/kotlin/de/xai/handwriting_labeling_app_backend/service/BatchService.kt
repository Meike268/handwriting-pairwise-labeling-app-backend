package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.ExamplePairInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.ReferenceSentenceInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.SampleInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.TaskBatchInfoBody
import de.xai.handwriting_labeling_app_backend.component.BatchConfigHandler
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.othersDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.OTHERS_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import kotlin.random.Random

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

    fun generateBatch(username: String): TaskBatchInfoBody {

        val user = userRepository.findByUsername(username)
        logger.info("Generating random batch for user $user")

        val config = configHandler.readBatchServiceConfig()


        val samplesDirectory = when (config.samplesOrigin) {
            XAI_SENTENCE_DIRECTORY_NAME -> xaiSentencesDirectory
            OTHERS_DIRECTORY_NAME -> othersDirectory
            else -> throw IllegalArgumentException("Sample origin is not supported. No directory corresponds to ${config.samplesOrigin}")
        }

        val batchQuestion = determineQuestionForBatch(user?.id!!, config, samplesDirectory)

        if (batchQuestion == null) {
            //ToDo: Handle case when user has no more unanswered samples
            throw IllegalStateException("No unanswered samples for this user and the questions prioritized in config.")
        }

        val unansweredSamples = getSamplesUserDidNotAnswerQuestionFor(user.id, batchQuestion, samplesDirectory)

        val batchReferenceSentence = determineReferenceSentenceForBatch(config, unansweredSamples)

        if (batchReferenceSentence == null) {
            //ToDo: Handle case when user has no more unanswered samples
            throw IllegalStateException("No unanswered samples for this user and the sentences prioritized in config.")
        }

        val examplePair = examplePairRepository.findByReferenceSentenceAndQuestion(batchReferenceSentence, batchQuestion)

        val samples = unansweredSamples.filter { sample ->
            sample.referenceSentence == batchReferenceSentence
        }.shuffled().take(config.batchSize).map { SampleInfoBody.fromSample(it) }

        return TaskBatchInfoBody(
            question = batchQuestion,
            referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(batchReferenceSentence),
            examplePair = ExamplePairInfoBody.fromExamplePair(examplePair!!),
            samples = samples
        )
    }

    private fun determineQuestionForBatch(userId: Long, config: BatchServiceConfig, samplesDirectory: File): Question? {

        val possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList()//config.prioritizedQuestions.toMutableList()

        while (possiblePrioritizedQuestions.isNotEmpty()) {
            val highestQuestionPriority = possiblePrioritizedQuestions.minOfOrNull { it.priority }
            val questionsWithPriority = possiblePrioritizedQuestions.filter { it.priority == highestQuestionPriority }

            val questionId = if (questionsWithPriority.isEmpty()) {
                throw IllegalStateException("No prioritized question to create batch for found.")
            } else if (questionsWithPriority.size == 1) {
                questionsWithPriority[0].questionId
            } else {
                possiblePrioritizedQuestions[Random.nextInt(1, questionsWithPriority.size)].questionId
            }

            val selectedQuestion = questionRepository.findById(questionId).get()

            val unansweredPossibleSamples = getSamplesUserDidNotAnswerQuestionFor(userId, selectedQuestion, samplesDirectory).filter { sample ->
                // remove also samples form reference sentences that are not in config
                sample.referenceSentence?.id in config.prioritizedReferenceSentences.map { it.referenceSentencesId }
            }

            if (unansweredPossibleSamples.isNotEmpty()) {
                return selectedQuestion
            } else {
                possiblePrioritizedQuestions.removeIf { it.questionId == questionId }
            }
        }
        // no question where the user has unanswered samples for
        return null
    }

    private fun getSamplesUserDidNotAnswerQuestionFor(userId: Long, batchQuestion: Question, samplesDirectory: File): List<Sample> {
        val answersToQuestionByUser = answerRepository.findAllByUserIdAndQuestionId(userId, batchQuestion.id!!)

        val unansweredSamples = sampleRepository.findAllInDirectoryRecursive(samplesDirectory).filter { sample ->
            !sampleHasQuestionAnswer(sample, answersToQuestionByUser)
        }

        return unansweredSamples
    }

    private fun sampleHasQuestionAnswer(sample: Sample, answers: List<Answer>): Boolean {
        for (answer in answers) {
            if (answer.sampleId == sample.id) {
                return true
            }
        }
        return false
    }

    private fun determineReferenceSentenceForBatch(config: BatchServiceConfig, unansweredSamples: List<Sample>): ReferenceSentence? {

        val availableReferenceSentenceIds = unansweredSamples.mapNotNull { it.referenceSentence?.id}.toSet()
        val possibleSentencePrios = config.prioritizedReferenceSentences.filter { it.referenceSentencesId in availableReferenceSentenceIds }

        if (possibleSentencePrios.isEmpty()) {
            return null
        }

        val totalPercentageFromAvailablePrios = possibleSentencePrios.map { it.priorityPercentage }.sum()

        val randomNumber = Random.nextInt(1, totalPercentageFromAvailablePrios)
        var currentBar = 0
        for (sentencePrio in possibleSentencePrios) {
            currentBar += sentencePrio.priorityPercentage
            if (randomNumber <= currentBar) {
                return referenceSentenceRepository.findById(sentencePrio.referenceSentencesId).get()
            }
        }
        return null
    }
}
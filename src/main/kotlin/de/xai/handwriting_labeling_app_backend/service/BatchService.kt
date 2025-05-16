package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.*
import de.xai.handwriting_labeling_app_backend.component.BatchConfigHandler
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_FINISHED
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_SUCCESS
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse
import kotlin.math.min

@Service
class BatchService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val exampleRepository: ExampleRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository,
    private val answerRepository: AnswerRepository,
    private val configHandler: BatchConfigHandler,
    private val taskService: TaskService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateBatch(
        username: String,
        excludedTasks: Map<Long, List<Long>> = emptyMap() // {questionId: List<sampleId>}
    ): GetBatchResponseBody {
        /**val allSamples = sampleRepository.findAll()
        for (sample in allSamples) {

        answerService.createOrUpdate(
        "user",
        sampleId = sample.id,
        questionId = 3,
        score = 3,
        submissionTimestamp = LocalDateTime.now()
        )
        }*/

        val config = configHandler.readBatchServiceConfig()

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")
        logger.info("Generating random batch for user $user")

        val taskBatchBody = if (config.samplesOrigin == XAI_SENTENCE_DIRECTORY_NAME)
            findXaiSentenceBatch(
                targetAnswerCount = config.targetAnswerCount,
                targetExpertAnswerCount = config.targetExpertAnswerCount,
                batchSize = config.batchSize,
                userId = user.id!!,
                forExpert = user.isExpert(),
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList(),
                excludedTasks = excludedTasks
            )
        else
            TODO()

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
     *          -> track count of
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
    private fun findXaiSentenceBatch(
        targetAnswerCount: Int,
        targetExpertAnswerCount: Int,
        batchSize: Int,
        userId: Long,
        forExpert: Boolean,
        possiblePrioritizedQuestions: MutableList<PrioritizedQuestion>,
        possiblePrioritizedSentences: MutableList<PrioritizedReferenceSentence>,
        excludedTasks: Map<Long, List<Long>>
    ): TaskBatchInfoBody? {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("User with ID $userId not found.")
        }

        val submittedAnswersCount = answerRepository.findByUserId(userId).size
        var pendingAnswersCount = 0

        val availableTasks = taskService.findAll(user)

        // Commented out: Prioritization logic
    //    val questionPriorities = possiblePrioritizedQuestions.map { prioritizedQuestion ->
    //        prioritizedQuestion to questionRepository.findById(prioritizedQuestion.questionId)
    //            .getOrElse {
    //                throw IllegalStateException("Question ${prioritizedQuestion.questionId} does not exist.")
    //            }
    //    }
    //
    //    val sentencePriorities = possiblePrioritizedSentences.map { prioritizedSentence ->
    //        prioritizedSentence to referenceSentenceRepository.findById(prioritizedSentence.referenceSentencesId)
    //            .getOrElse {
    //                throw IllegalStateException("Sentence ${prioritizedSentence.referenceSentencesId} does not exist.")
    //            }
    //    }
    //
    //    val questionAndSentencePriorities = sentencePriorities.flatMap { (sentencePrio, sentence) ->
    //        questionPriorities.map { (questionPrio, question) ->
    //            Pair(question, sentence) to questionPrio.priority + sentencePrio.priority
    //        }
    //    }.shuffled().sortedBy { it.second }

        var firstFoundBatch: TaskBatchInfoBody? = null

        // Commented out: Full priority-based loop
    //    for ((questionAndSentence, _) in questionAndSentencePriorities) {
    //        val (question, sentence) = questionAndSentence
    //        if (question !in sentence.applicableQuestions!!) {
    //            continue
    //        }
    //
    //        val questionAnswers = answerRepository.findAllByQuestionId(question.id!!)
    //
    //        val availableTasks = taskService.findAll()
    //            .filter {
    //                it.question.id == question.id
    //                        && it.sample1.referenceSentence?.id == sentence.id
    //                        && it.sample2.referenceSentence?.id == sentence.id
    //                        && excludedTasks[question.id]?.contains(it.sample1.id) != true
    //                        && excludedTasks[question.id]?.contains(it.sample2.id) != true
    //            }
    //            .map { task -> task to questionAnswers.filter { it.sampleId == task.sample.id } }
    //            .filter { (_, answersOfTask) ->
    //                notAnsweredByUser(answersOfTask, userId)
    //                        && (missingAnswerFromAnyone(answersOfTask, targetAnswerCount)
    //                        || (forExpert && missingAnswerFromExpert(answersOfTask, targetExpertAnswerCount)))
    //            }
    //
    //        pendingAnswersCount += availableTasks.size
    //
    //        if (firstFoundBatch != null || availableTasks.isEmpty()) {
    //            continue
    //        }
    //
    //        val batchTasks = availableTasks
    //            .shuffled()
    //            .sortedBy { (_, answers) -> answers.size }
    //            .sortedBy { (_, answers) -> if (forExpert) min(targetExpertAnswerCount, answers.filter { it.isFromExpert() }.size) else null }
    //            .take(batchSize)
    //            .map { (task, _) ->
    //                Pair(SampleInfoBody.fromSample(task.sample1), SampleInfoBody.fromSample(task.sample2))
    //            }
    //
    //        val example = question.exampleImageName?.let { exampleRepository.findByImageName(it) }
    //            ?: throw IllegalStateException("Could not retrieve Example for image with name ${question.exampleImageName}")
    //
    //        firstFoundBatch = TaskBatchInfoBody(
    //            userAnswerCounts = GetUserAnswerCountsBody(
    //                submittedAnswersCount = submittedAnswersCount,
    //                pendingAnswersCount = null
    //            ),
    //            question = question,
    //            example = example,
    //            referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(sentence),
    //            samplePairs = batchTasks
    //        )
    //    }

        // New simplified logic using ASAP-based task list directly
        val groupedByQuestionAndSentence = availableTasks.groupBy { Triple(it.question, it.sample1.referenceSentence, it.sample2.referenceSentence) }

        for ((key, tasks) in groupedByQuestionAndSentence) {
            val (question, sentence1, sentence2) = key
            val sentence = sentence1 // assume both are same

            if (sentence == null || question !in sentence.applicableQuestions ?: emptyList()) {
                continue
            }

            pendingAnswersCount += tasks.size

            if (firstFoundBatch != null || tasks.isEmpty()) {
                continue
            }

            val batchTasks = tasks
                .take(batchSize)
                .map { task ->
                    Pair(SampleInfoBody.fromSample(task.sample1), SampleInfoBody.fromSample(task.sample2))
                }

            val example = question.exampleImageName?.let { exampleRepository.findByImageName(it) }
                ?: throw IllegalStateException("Could not retrieve Example for image with name ${question.exampleImageName}")

            firstFoundBatch = TaskBatchInfoBody(
                userAnswerCounts = GetUserAnswerCountsBody(
                    submittedAnswersCount = submittedAnswersCount,
                    pendingAnswersCount = null
                ),
                question = question,
                example = example,
                referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(sentence),
                samplePairs = batchTasks
            )
        }

        firstFoundBatch?.userAnswerCounts?.pendingAnswersCount = pendingAnswersCount

        return firstFoundBatch
    }



    private fun notAnsweredByUser(answers: List<Answer>, userId: Long): Boolean {
    return answers.none { it.user?.id == userId }
    }


    private fun missingAnswerFromAnyone(
        answers: List<Answer>,
        targetAnswerCount: Int
    ) = answers.size < targetAnswerCount

    private fun missingAnswerFromExpert(
        answers: List<Answer>,
        targetExpertAnswerCount: Int
    ) = answers.filter { it.isFromExpert() }.size < targetExpertAnswerCount
}
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
    private val taskService: TaskService,
    private val userBatchLogRepository: UserBatchLogRepository,
    private val comparisonListRepository: ComparisonListRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateBatch(
        username: String,
    ): GetBatchResponseBody {

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")
        logger.info("Generating random batch for user $user")

        val config = configHandler.readBatchServiceConfig()

        /*
        val batchLimit = config.batchCount
        val batchSize = config.batchSize
        val userBatchCount = userBatchLogRepository.countByUserId(user.id!!)
        val userAnswerCount = answerRepository.findByUserId(user.id!!).size

        if (userAnswerCount >= batchLimit * batchSize) {
            logger.info("User ${user.username} reached batch limit ($batchLimit).")
            return GetBatchResponseBody(state = GET_BATCH_RESPONSE_STATE_FINISHED, body = null)
        }

        */

        val taskBatchBody = if (config.samplesOrigin == XAI_SENTENCE_DIRECTORY_NAME)
            findXaiSentenceBatch(
                targetAnswerCount = config.targetAnswerCount,
                targetExpertAnswerCount = config.targetExpertAnswerCount,
                batchSize = config.batchSize,
                userId = user.id!!,
                username = username,
                forExpert = user.isExpert(),
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList(),
                // excludedTasks = excludedTasks
            )

        else
            TODO()

        logger.info("taskBatchBody: $taskBatchBody")

        if (taskBatchBody == null) {
            return GetBatchResponseBody(state = GET_BATCH_RESPONSE_STATE_FINISHED, body = null)
        }

        /*//
        var hasSavedLog = false// Check if user has already a batch log entry
        if (!hasSavedLog){
            userBatchLogRepository.save(UserBatchLog(user = user))
            hasSavedLog = true
        }
        */



        val response =  GetBatchResponseBody(
            state = GET_BATCH_RESPONSE_STATE_SUCCESS,
            body = taskBatchBody
        )

        logger.info("response: $response")

        return response
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
        username: String,
        forExpert: Boolean,
        possiblePrioritizedQuestions: MutableList<PrioritizedQuestion>,
        possiblePrioritizedSentences: MutableList<PrioritizedReferenceSentence>,
    ): TaskBatchInfoBody? {
        val config = configHandler.readBatchServiceConfig()

        val submittedAnswersCount = answerRepository.findByUserId(userId).size
        var pendingAnswersCount = 0

        val availableTasks = taskService.findAll(username)
        pendingAnswersCount += availableTasks.size

        val validTasks = availableTasks.filter { task ->
            val ref1 = task.sample1.referenceSentence
            val ref2 = task.sample2.referenceSentence
            ref1 != null && ref2 != null &&
            ref1.isQuestion1Applicable() && ref2.isQuestion1Applicable()
        }


        if (validTasks.isEmpty()) return null


       val selectedTasks = validTasks
            .shuffled()
            //.sortedBy { ... }
            .take(batchSize)

       selectedTasks.forEach { task ->
            val sample1Id = task.sample1.id
            val sample2Id = task.sample2.id

            comparisonListRepository.updateAnnotatedBySampleIds(sample1Id, sample2Id, true)
        }

        val samplePairs = selectedTasks.map { task ->
            Pair(
                SampleInfoBody.fromSample(task.sample1),
                SampleInfoBody.fromSample(task.sample2)
            )
        }

        val firstTask = selectedTasks.first()

        val example = firstTask.question.exampleImageName?.let { exampleRepository.findByImageName(it) }
            ?: throw IllegalStateException("Could not retrieve Example for image with name ${firstTask.question.exampleImageName}")

        val firstFoundBatch = TaskBatchInfoBody(
            userAnswerCounts = GetUserAnswerCountsBody(
                submittedAnswersCount = submittedAnswersCount,
                pendingAnswersCount = pendingAnswersCount
            ),
            question = firstTask.question,
            example = example,
            samplePairs = samplePairs
        )

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
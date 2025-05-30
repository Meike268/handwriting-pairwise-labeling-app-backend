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
    private val userBatchLogRepository: UserBatchLogRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateBatch(
        username: String,
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

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")
        logger.info("Generating random batch for user $user")

        val batchLimit = 25  // Each annotator does a maximum of 25 batches
        val userBatchCount = userBatchLogRepository.countByUserId(user.id!!)

        if (userBatchCount >= batchLimit) {
            logger.info("User ${user.username} reached batch limit ($batchLimit).")
            return GetBatchResponseBody(state = GET_BATCH_RESPONSE_STATE_FINISHED, body = null)
        }


        val config = configHandler.readBatchServiceConfig()

        val taskBatchBody = if (config.samplesOrigin == XAI_SENTENCE_DIRECTORY_NAME)
            findXaiSentenceBatch(
                targetAnswerCount = config.targetAnswerCount,
                targetExpertAnswerCount = config.targetExpertAnswerCount,
                batchSize = config.batchSize,
                userId = user.id,
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

        userBatchLogRepository.save(UserBatchLog(user = user))

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
        val submittedAnswersCount = answerRepository.findByUserId(userId).size
        var pendingAnswersCount = 0

        val availableTasks = taskService.findAll(username)
        // val availableTasks = emptyList<Task>() // for testing

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

        logger.info("availableTasks: $availableTasks")

        val validTasks = availableTasks.filter { task ->
            val ref1 = task.sample1.referenceSentence
            val ref2 = task.sample2.referenceSentence
            ref1 != null && ref2 != null &&
            ref1.isQuestion1Applicable() && ref2.isQuestion1Applicable()
        }

        logger.info("validTasks: $validTasks")

        if (validTasks.isEmpty()) return null

        val samplePairs = validTasks.map { task ->
            Pair(
                SampleInfoBody.fromSample(task.sample1),
                SampleInfoBody.fromSample(task.sample2)
            )
        }

        val firstTask = validTasks.first()

        val example = firstTask.question.exampleImageName?.let { exampleRepository.findByImageName(it) }
            ?: throw IllegalStateException("Could not retrieve Example for image with name ${firstTask.question.exampleImageName}")

        val firstFoundBatch = TaskBatchInfoBody(
            userAnswerCounts = GetUserAnswerCountsBody(
                submittedAnswersCount = submittedAnswersCount,
                pendingAnswersCount = samplePairs.size
            ),
            question = firstTask.question,
            example = example,
            samplePairs = samplePairs
        )

        logger.info("firstFoundBatch: $firstFoundBatch")

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
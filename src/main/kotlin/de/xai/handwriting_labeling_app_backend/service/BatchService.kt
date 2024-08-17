package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.*
import de.xai.handwriting_labeling_app_backend.component.BatchConfigHandler
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_FINISHED
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_SUCCESS
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.OTHERS_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.ROLE_EXPERT
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.ROLE_USER
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.othersDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import kotlin.jvm.optionals.getOrElse

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

        val samplesDirectory = when (config.samplesOrigin) {
            XAI_SENTENCE_DIRECTORY_NAME -> xaiSentencesDirectory
            OTHERS_DIRECTORY_NAME -> othersDirectory
            else -> throw IllegalArgumentException("Sample origin is not supported. No directory corresponds to ${config.samplesOrigin}")
        }

        val userRoles = user.roles.mapNotNull { it.name }
        val taskBatchBodyForExpert = if (ROLE_EXPERT in userRoles) {
            // user is expert -> first try to generate a batch of samples where expert answer is missing
            findBatch(
                targetAnswerCount = config.targetAnswerCount,
                batchSize = config.batchSize,
                samplesDirectory = samplesDirectory,
                userId = user.id!!,
                userRole = ROLE_EXPERT,
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList(),
                excludedTasks = excludedTasks
            )
        } else null
        // if no batch for expert answers was assembled, then create batch where user(=any) answer is missing
        val taskBatchBody = taskBatchBodyForExpert
            ?: findBatch(
                targetAnswerCount = config.targetExpertAnswerCount,
                batchSize = config.batchSize,
                samplesDirectory = samplesDirectory,
                userId = user.id!!,
                userRole = ROLE_USER,
                possiblePrioritizedQuestions = config.prioritizedQuestions.toMutableList(),
                possiblePrioritizedSentences = config.prioritizedReferenceSentences.toMutableList(),
                excludedTasks = excludedTasks
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
    fun findBatch(
        targetAnswerCount: Int,
        batchSize: Int,
        samplesDirectory: File,
        userId: Long,
        userRole: String,
        possiblePrioritizedQuestions: MutableList<PrioritizedQuestion>,
        possiblePrioritizedSentences: MutableList<PrioritizedReferenceSentence>,
        excludedTasks: Map<Long, List<Long>>
    ): TaskBatchInfoBody? {
        val logStartTime = System.currentTimeMillis()
        logger.info("Generating random batch for user $userId with role $userRole")
        // get questions and reference sentence that are stored in DB
        val priorityToQuestionPairsSorted = possiblePrioritizedQuestions.map { prioritizedQuestion ->
            prioritizedQuestion to questionRepository.findById(prioritizedQuestion.questionId).getOrElse {
                throw IllegalStateException("Question ${prioritizedQuestion.questionId} does not exist.")
            }
        }.shuffled().sortedBy { it.first.priority } // shuffle before sort, to randomly pick between same priority

        val priorityToSentencePairsSorted = possiblePrioritizedSentences.map { prioritizedSentence ->
            prioritizedSentence to referenceSentenceRepository.findById(prioritizedSentence.referenceSentencesId)
                .getOrElse {
                    throw IllegalStateException("Sentence ${prioritizedSentence.referenceSentencesId} does not exist.")
                }
        }.shuffled().sortedBy { it.first.priority } // shuffle before sort, to randomly pick between same priority

        val submittedAnswersCount = answerRepository.findByUserId(userId).size
        var pendingAnswersCount = 0

        var firstFoundBatchForUser: TaskBatchInfoBody? = null


        val comboTimes = mutableListOf<Long>()
        var timeReducingAllSamplesToRefSent: Long = 0
        var timeFilterRefSentSamplesForNotAnsweredByUser: Long = 0
        var timeGetAnswersWithCountsFromDB: Long = 0
        var timeMapCountsOntoRefSentSamplesNotAnsweredByUser: Long = 0
        var timeCountPendingAnswersOfUser: Long = 0
        var timeToFindBatchFromSampleToCountMap: Long = 0


        for (prioToQuestion in priorityToQuestionPairsSorted) {
            for (prioToSentence in priorityToSentencePairsSorted) {

                val question = prioToQuestion.second
                val sentence = prioToSentence.second
                logger.info("QuestinID: ${question.id}, sentId: ${sentence.id}")

                val taskStartTime = System.currentTimeMillis()

                if (question !in sentence.applicableQuestions!!) {
                    // question not applicable to sentence, continue with next sentence
                    val timeInLoop = System.currentTimeMillis() - taskStartTime
                    comboTimes.addLast(timeInLoop)
                    logger.info("Finished task (not applicable) questionId: ${question.id}, sentId: ${sentence.id} in ${timeInLoop}ms")
                    continue
                }

                // All tasks with their corresponding answers
                val tasksToAnswers = doAndAddTimer(
                    addToTimer = { time -> timeReducingAllSamplesToRefSent += time }
                ) {
                    val tasks = taskService.findAll().filter { it.question.id == question.id && it.sample.referenceSentence?.id == sentence.id }
                    val questionAnswers = answerRepository.findAllByQuestionId(question.id!!)
                    tasks.map { task -> task to questionAnswers.filter { it.sampleId == task.sample.id } }
                }


                val tasksNotAnsweredByUser = doAndAddTimer(
                    addToTimer = { time -> timeFilterRefSentSamplesForNotAnsweredByUser += time }
                ) {
                    tasksToAnswers.filter { ( _, answers ) -> !answers.any { it.user?.id == userId } }
                }
                if (tasksNotAnsweredByUser.isEmpty()) {
                    // the user answered all samples for this sentence and question, none pending
                    val timeInLoop = System.currentTimeMillis() - taskStartTime
                    comboTimes.addLast(timeInLoop)
                    logger.info("Finished task (No unanswered by user) questionId: ${question.id}, sentId: ${sentence.id} in ${System.currentTimeMillis() - taskStartTime}ms")
                    continue
                }

                val samplesToAnswerCount = doAndAddTimer(
                    addToTimer = { time -> timeMapCountsOntoRefSentSamplesNotAnsweredByUser += time }
                ) {
                    tasksNotAnsweredByUser.map { (task, answers) ->
                        task.sample to answers.size
                    }
                }

                // count all tasks (question = sample) where that the user could give answer to.
                // That means the user did not answer yet and the target number of answers is not fulfilled.
                val pendingCount = doAndAddTimer(
                    addToTimer = { time -> timeCountPendingAnswersOfUser += time }
                ) {
                    val sampleIdsUserAnsweredQuestionFor = tasksNotAnsweredByUser.map { (task, _) -> task.sample.id }
                    samplesToAnswerCount.filter { pair ->
                        pair.first.id in sampleIdsUserAnsweredQuestionFor && pair.second < targetAnswerCount
                    }.size
                }

                // count pending answers for user in this combination of question and sentence
                pendingAnswersCount += pendingCount
                if (firstFoundBatchForUser != null) {
                    // the batch for the user is already found. We only iterate the questions and sentences further to
                    // count pending answers of the user
                    val timeInLoop = System.currentTimeMillis() - taskStartTime
                    comboTimes.addLast(timeInLoop)
                    logger.info("Finished task (batch already found) questionId: ${question.id}, sentId: ${sentence.id} in ${System.currentTimeMillis() - taskStartTime}ms")
                    continue
                }

                // start by collecting at least one answer per sample. If all samples have one answer, then collect answers
                // until every sample has 2 answers, .... until every sample has targetAnswerCount samples.
                // Then stop collecting answers for this combo of sentence and question
                doAndAddTimer(
                    addToTimer = { time -> timeToFindBatchFromSampleToCountMap += time }
                ) {
                    for (answerCount in 1..targetAnswerCount) {
                        val samplesToMakeBatchFrom = samplesToAnswerCount.filter { sampleToCount ->
                            sampleToCount.second < answerCount
                        }.map { sampleToCount -> sampleToCount.first }

                        if (samplesToMakeBatchFrom.isNotEmpty()) {
                            val batchSamples = samplesToMakeBatchFrom.shuffled().take(batchSize)
                            val example = question.exampleImageName?.let { exampleRepository.findByImageName(it) }
                                ?: throw IllegalStateException("Could not retrieve Example for image with name ${question.exampleImageName}")
                            firstFoundBatchForUser = TaskBatchInfoBody(
                                question = question,
                                example = example,
                                referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(sentence),
                                samples = batchSamples.map { SampleInfoBody.fromSample(it) },
                                userAnswerCounts = GetUserAnswerCountsBody(
                                    submittedAnswersCount = submittedAnswersCount,
                                    pendingAnswersCount = null
                                )
                            )
                        }
                    }
                }
                val timeInLoop = System.currentTimeMillis() - taskStartTime
                comboTimes.addLast(timeInLoop)
                logger.info("Finished task (BATCH CREATED) questionId: ${question.id}, sentId: ${sentence.id} in ${System.currentTimeMillis() - taskStartTime}ms")
            }
        }
        // now we iterated all feasible combinations of question and sentence and counted pending answers for this user
        firstFoundBatchForUser?.userAnswerCounts?.pendingAnswersCount = pendingAnswersCount

        logger.info("Batch generation for user with id ${userId} took ${System.currentTimeMillis() - logStartTime} ms")
        logger.info(
            "Spent time on operations:\n" +
                    "timeReducingAllSamplesToRefSent: $timeReducingAllSamplesToRefSent\n" +
                    "timeFilterRefSentSamplesForNotAnsweredByUser: $timeFilterRefSentSamplesForNotAnsweredByUser\n" +
                    "timeGetAnswersWithCountsFromDB: $timeGetAnswersWithCountsFromDB\n" +
                    "timeMapCountsOntoRefSentSamplesNotAnsweredByUser: $timeMapCountsOntoRefSentSamplesNotAnsweredByUser\n" +
                    "timeCountPendingAnswersOfUser: $timeCountPendingAnswersOfUser\n" +
                    "timeToFindBatchFromSampleToCountMap: $timeToFindBatchFromSampleToCountMap\n" +
                    "sum of loops: ${comboTimes.sum()}"
        )

        return firstFoundBatchForUser
    }

    fun <B> doAndAddTimer(addToTimer: (Long) -> Unit, function: () -> B): B {
        val startTime = System.currentTimeMillis()
        val result = function()
        addToTimer(System.currentTimeMillis() - startTime)
        return result
    }
}
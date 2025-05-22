package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.*
import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.othersDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import de.xai.handwriting_labeling_app_backend.utils.safeLet
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import de.xai.handwriting_labeling_app_backend.service.UserComparisonMatrixService


@Service
class AnswerService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val answerRepository: AnswerRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository,
    private val sampleRepository: SampleRepository,
    private val userComparisonMatrixService: UserComparisonMatrixService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createOrUpdate(
        username: String,
        sampleId1: Long,
        sampleId2: Long,
        questionId: Long,
        score: Int,
        submissionTimestamp: LocalDateTime
    ): Answer {
        val user = userRepository.findByUsername(username)!!
        val question = questionRepository.findById(questionId).get()

        // record result in userComparisonMatrix
        if (score != 0) {
            val winnerId = if (score == 1) sampleId1 else sampleId2
            val loserId = if (score == 1)  sampleId2 else sampleId1
            userComparisonMatrixService.recordComparison(user, winnerId, loserId, size = 99)
        }

        return answerRepository.save(
            Answer(
                user = user,
                sampleId1 = sampleId1,
                sampleId2 = sampleId2,
                question = question,
                score = score,
                submissionTimestamp = submissionTimestamp
            )
        )
    }

    fun getAnswers(
        includeXaiSamplesInExport: Boolean,
        includeOthersSamplesInExport: Boolean
    ): ExportAnswersBody {
        logger.debug("Prepare export of answers. includeXai = $includeXaiSamplesInExport, includeOthers = $includeOthersSamplesInExport")
        val startTime = System.currentTimeMillis()
        var xaiMetaData: XAiExportAnswersMetaData? = null
        var xaiAnswerInfos: List<XAiExportAnswerInfoBody>? = null
        var othersAnswerInfos: List<OthersExportAnswersBody>? = null

        if (includeXaiSamplesInExport) {
            xaiAnswerInfos = retrieveXaiAnswers()
            xaiMetaData = retrieveXaiMetaData(xaiAnswerInfos)
        }

        if (includeOthersSamplesInExport) {
            othersAnswerInfos = retrieveOthersAnswers()
        }

        logger.debug("Creating the answer export body took ${System.currentTimeMillis() - startTime} ms")
        return ExportAnswersBody(
            xaiMetaData = xaiMetaData,
            xaiAnswers = xaiAnswerInfos,
            othersAnswers = othersAnswerInfos
        )
    }


    private fun retrieveXaiAnswers(): List<XAiExportAnswerInfoBody> {
        val allXaiSamples = sampleRepository.findAllInDirectoryRecursive(xaiSentencesDirectory)

        return answerRepository.findAll().mapNotNull { answer ->
            // Find two samples based on sampleId1 and sampleId2
            val sample1 = answer.sampleId1?.let { sampleId1 ->
                allXaiSamples.find { sample -> sample.id == sampleId1 }
            }
            val sample2 = answer.sampleId2?.let { sampleId2 ->
                allXaiSamples.find { sample -> sample.id == sampleId2 }
            }

            // If either sample is not found, return null (skip this answer)
            if (sample1 == null || sample2 == null) {
                return@mapNotNull null
            }

            // Retrieve the reference sentence IDs from both samples
            val referenceSentenceId1 = sample1.referenceSentence?.id
            val referenceSentenceId2 = sample2.referenceSentence?.id

            // If either sample is missing a reference sentence, skip this answer
            if (referenceSentenceId1 == null || referenceSentenceId2 == null) {
                return@mapNotNull null
            }

            safeLet(
                answer.user?.id,
                answer.sampleId1,
                answer.sampleId2,
                answer.question?.id,
                answer.score,
                answer.submissionTimestamp
            ) { userId, sampleId1, sampleId2, questionId, score, time ->

                XAiExportAnswerInfoBody(
                    userId = userId,
                    sampleId1 = sampleId1,
                    sampleId2 = sampleId2,
                    referenceSentenceId1 = referenceSentenceId1,
                    referenceSentenceId2 = referenceSentenceId2,
                    questionId = questionId,
                    score = score,
                    submissionTimestamp = time.toString()
                )
            }
                ?: throw IllegalStateException(
                    "Problem constructing ${XAiExportAnswerInfoBody::class.java.name} from " +
                            "answers in DB.\n Answer: $answer"
                )
        }
    }

    private fun retrieveXaiMetaData(
        xaiAnswerInfos: List<XAiExportAnswerInfoBody>
    ): XAiExportAnswersMetaData {
        val questionIds = questionRepository.findAll().mapNotNull { it.id }
        val referenceSentences = referenceSentenceRepository.findAll()
        val userInfos = userRepository.findAll().mapNotNull { UserInfoBody.fromUser(it) }

        return XAiExportAnswersMetaData(
            questionIds = questionIds,
            referenceSentences = referenceSentences,
            userInfos = userInfos,
            samplesDirectoryName = XAI_SENTENCE_DIRECTORY_NAME,
            samplesCount = xaiAnswerInfos.size
        )
    }

    fun retrieveOthersAnswers(): List<OthersExportAnswersBody> {
        val allOthersSamples = sampleRepository.findAllInDirectoryRecursive(othersDirectory)

        return answerRepository.findAll().mapNotNull { answer ->
            // Find the first sample using sampleId1
            val sample1 = answer.sampleId1?.let { sampleId1 ->
                allOthersSamples.find { sample -> sample.id == sampleId1 }
            }

            // Find the second sample using secondarySampleId or other logic (adjust this as needed)
            val sample2 = answer.sampleId2?.let { sampleId2 ->
                allOthersSamples.find { sample -> sample.id == sampleId2 }
            }

            // If either sample is not found, return null (skip this answer)
            if (sample1 == null || sample2 == null) {
                return@mapNotNull null
            }

            safeLet(
                answer.user?.id,
                answer.sampleId1,
                answer.sampleId2,
                answer.score
            ) { userId, sampleId1, sampleId2, score ->

                OthersExportAnswersBody(
                    userId = userId,
                    sampleId1 = sampleId1,
                    sampleId2 = sampleId2,
                    score = score
                )
            }
                ?: throw IllegalStateException(
                    "Problem constructing ${OthersExportAnswersBody::class.java.name} from " +
                            "answers in DB.\n Answer: $answer"
                )
        }
    }


    fun deleteAnswersOfSample(sampleId: Long): Boolean {
        val answersToSample = answerRepository.findAllBySampleId(sampleId)
        val answersToDeleteCount = answersToSample.size
        logger.debug("Delete all ${answersToDeleteCount} answers that were give to sample with id: $sampleId.")
        var deletedCount = 0
        for (answer in answersToSample) {
            try {
                answerRepository.delete(answer)
                deletedCount++
            } catch (e: Exception) {
                logger.debug("Exception occurred while deleting {}: {}", answer, e.message)
            }
        }
        return answersToDeleteCount == deletedCount
    }

}
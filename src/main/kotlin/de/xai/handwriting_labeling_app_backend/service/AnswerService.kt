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

@Service
class AnswerService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val answerRepository: AnswerRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository,
    private val sampleRepository: SampleRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createOrUpdate(
        username: String,
        sampleId: Long,
        questionId: Long,
        score: Int,
        submissionTimestamp: LocalDateTime
    ): Answer {
        return answerRepository.save(
            Answer(
                user = userRepository.findByUsername(username)!!,
                sampleId = sampleId,
                question = questionRepository.findById(questionId).get(),
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
            // search sample of this answer in samples/xai_sentences and read ref sent id from it
            val referenceSentenceId = answer.sampleId?.let { sampleId ->
                allXaiSamples.find { sample ->
                    sample.id == sampleId
                }?.referenceSentence?.id
            } ?: return@mapNotNull null //the sample this answer refers to is not in xai directory

            safeLet(
                answer.user?.id,
                answer.sampleId,
                answer.question?.id,
                answer.score,
                answer.submissionTimestamp
            ) { userId, sampleId, questionId, score, time ->

                XAiExportAnswerInfoBody(
                    userId = userId,
                    sampleId = sampleId,
                    referenceSentenceId = referenceSentenceId,
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
            // search sample of this answer in samples/others
            answer.sampleId?.let { sampleId ->
                allOthersSamples.find { sample ->
                    sample.id == sampleId
                }
            } ?: return@mapNotNull null //the sample this answer refers to is not in others directory

            safeLet(
                answer.user?.id,
                answer.sampleId,
                answer.score
            ) { userId, sampleId, score ->

                OthersExportAnswersBody(
                    userId = userId,
                    sampleId = sampleId,
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
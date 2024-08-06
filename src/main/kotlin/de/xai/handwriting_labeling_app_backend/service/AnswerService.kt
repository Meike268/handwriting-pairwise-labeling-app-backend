package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.ExportAnswerInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.ExportAnswersBody
import de.xai.handwriting_labeling_app_backend.apimodel.ExportAnswersMetaData
import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.repository.*
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import de.xai.handwriting_labeling_app_backend.utils.safeLet
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
    fun createOrUpdate(username: String, sampleId: Long, questionId: Long, score: Int): Answer {
        return answerRepository.save(
            Answer(
                user = userRepository.findByUsername(username)!!,
                sampleId = sampleId,
                question = questionRepository.findById(questionId).get(),
                score = score,
                submissionTimestamp = LocalDateTime.now()
            )
        )
    }

    fun getAnswersForXAiSentences(): ExportAnswersBody {
        val questionIds = questionRepository.findAll().mapNotNull { it.id }
        val referenceSentences = referenceSentenceRepository.findAll()

        val answerInfos = answerRepository.findAll().mapNotNull { answer ->
            // search sample of this answer in xai_sentences and read ref sent id from it
            val referenceSentenceId = answer.sampleId?.let { sampleId ->
                sampleRepository.findAllInDirectoryRecursive(xaiSentencesDirectory).find { sample ->
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

                ExportAnswerInfoBody(
                    userId = userId,
                    sampleId = sampleId,
                    referenceSentenceId = referenceSentenceId,
                    questionId = questionId,
                    score = score,
                    submissionTimestamp = time.toString()
                )
            }
                ?: throw IllegalStateException("Problem constructing ${ExportAnswerInfoBody::class.java.name} from " +
                        "answers in DB.\n Answer: $answer")
        }

        return ExportAnswersBody(
            metaData = ExportAnswersMetaData(
                questionIds = questionIds,
                referenceSentences = referenceSentences,
                samplesDirectoryName = XAI_SENTENCE_DIRECTORY_NAME,
                samplesCount = answerInfos.size
            ),
            answers = answerInfos
        )
    }
}
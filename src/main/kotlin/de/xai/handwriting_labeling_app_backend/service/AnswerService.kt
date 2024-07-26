package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.repository.*
import org.springframework.stereotype.Service

@Service
class AnswerService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val answerRepository: AnswerRepository,
) {
    fun createOrUpdate(username: String, sampleId: Long, questionId: Long, score: Int): Answer {
        return answerRepository.save(Answer(
            user = userRepository.findByUsername(username)!!,
            sampleId = sampleId,
            question = questionRepository.findById(questionId).get(),
            score = score
        ))
    }
}
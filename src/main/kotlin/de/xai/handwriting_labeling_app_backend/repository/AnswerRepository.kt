package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Answer
import org.springframework.data.jpa.repository.JpaRepository

interface AnswerRepository: JpaRepository<Answer, Long> {
    fun findAllByUserIdAndQuestionId(userId: Long, questionId: Long): List<Answer>

    fun findAllByQuestionIdAndSampleId(questionId: Long, sampleId: Long): List<Answer>

    fun findAllBySampleId(sampleId: Long): List<Answer>
}
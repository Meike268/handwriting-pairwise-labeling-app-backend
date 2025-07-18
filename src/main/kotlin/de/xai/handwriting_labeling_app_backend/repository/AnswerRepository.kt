package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Answer
import org.springframework.data.jpa.repository.JpaRepository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface AnswerRepository: JpaRepository<Answer, Long> {
    fun findAllByQuestionId(questionId: Long): List<Answer>

    fun findByUserId(userId: Long): List<Answer>

    fun findAllBySampleId1(sampleId1: Long): List<Answer>

    fun findAllBySampleId2(sampleId2: Long): List<Answer>

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Answer a
        WHERE (a.sampleId1 = :sample1Id AND a.sampleId2 = :sample2Id)
           OR (a.sampleId1 = :sample2Id AND a.sampleId2 = :sample1Id)
    """)
    fun existsBySamplePair(sample1Id: Long, sample2Id: Long): Boolean


}


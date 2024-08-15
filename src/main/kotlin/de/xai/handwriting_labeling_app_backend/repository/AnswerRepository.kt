package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.model.SampleIdToAnswerCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AnswerRepository: JpaRepository<Answer, Long> {
    fun findByUserId(userId: Long): List<Answer>

    fun findAllByUserIdAndQuestionId(userId: Long, questionId: Long): List<Answer>

    fun findAllBySampleId(sampleId: Long): List<Answer>

    @Query(value = "SELECT sample_id, COUNT(*) as answer_count\n" +
            "FROM answer\n" +
            "JOIN user u on answer.user_id = u.id\n" +
            "JOIN user_role ur on u.id = ur.user_id\n" +
            "JOIN role r on r.id = ur.role_id\n" +
            "WHERE answer.question_id = 1 AND r.name = 'ROLE_ADMIN'\n" +
            "GROUP BY sample_id;", nativeQuery = true)
    fun countAnswerPerSampleForQuestionAndRole(
        @Param("questionId") questionId: String,
        @Param("role") role: String): List<SampleIdToAnswerCount>

}


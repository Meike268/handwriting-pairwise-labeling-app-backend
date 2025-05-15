package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "answer")
@IdClass(AnswerId::class)
class Answer(

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var user: User? = null,

    @Column(name = "sample_id_1")
    var sampleId1: Long? = null,

    @Column(name = "sample_id_2")
    var sampleId2: Long? = null,

    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var question: Question? = null,

    @Column(name = "score")
    var score: Int? = null,

    @Column(name = "submission_timestamp")
    val submissionTimestamp: LocalDateTime? = null,
) {
    fun isFromExpert(): Boolean = user?.isExpert() ?: false
}

data class AnswerId(
    private val sampleId: Long? = null,
    private val question: Question? = null,
    private val user: User? = null
) : Serializable
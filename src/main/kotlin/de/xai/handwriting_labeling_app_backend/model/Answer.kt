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

    @Column(name = "sample_id")
    var sampleId: Long? = null,

    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var question: Question? = null,

    @Column(name = "score")
    var score: Int? = null,

    @Column(name = "submission_timestamp")
    val submissionTimestamp: LocalDateTime? = null,
)

data class AnswerId(
    private val sampleId: Long? = null,
    private val question: Question? = null,
    private val user: User? = null
) : Serializable
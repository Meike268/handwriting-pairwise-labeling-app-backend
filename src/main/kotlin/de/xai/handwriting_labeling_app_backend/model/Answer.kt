package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "answer")
@IdClass(AnswerId::class)
class Answer(

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var user: User? = null,

    @JoinColumn(name = "sample_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var sample: Sample? = null,

    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var question: Question? = null,

    @Column(name = "score")
    var score: Int? = null,
)

class AnswerId(
    private val sample: Sample,
    private val question: Question,
    private val user: User
) : Serializable
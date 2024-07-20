package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import jakarta.persistence.*

@Entity
@IdClass(AnswerId::class)
class Answer(

    @Column
    var score: Int? = null,

    @JoinColumn(name = "sample_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var sample: Sample? = null,

    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var question: Question? = null,

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Id
    @ManyToOne
    var user: User? = null,
)

class AnswerId(
    private val sample: Sample,
    private val question: Question,
    private val user: User) : Serializable
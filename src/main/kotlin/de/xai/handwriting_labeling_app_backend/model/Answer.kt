package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(AnswerId::class)
class Answer(

    @Column
    var score: Int? = null,

    @Column
    @Id
    @ManyToOne
    var task: Task? = null,

    @Column
    @Id
    @ManyToOne
    var user: User? = null,
)

class AnswerId(
    private val task: Task,
    private val user: User) : Serializable
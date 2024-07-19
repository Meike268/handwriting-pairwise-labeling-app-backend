package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import jakarta.persistence.*

@Entity
@IdClass(TaskId::class)
class Task(

    @JoinColumn
    @Id
    @ManyToOne
    var sample: Sample? = null,

    @JoinColumn
    @Id
    @ManyToOne
    var question: Question? = null,
)

class TaskId(
    private val sample: Sample,
    private val question: Question) : Serializable
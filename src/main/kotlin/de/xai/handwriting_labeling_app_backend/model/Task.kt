package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(TaskId::class)
class Task(

    @Column
    @Id
    @ManyToOne
    var sample: Sample? = null,

    @Column
    @Id
    @ManyToOne
    var question: Question? = null,
)

class TaskId(
    private val sample: Sample,
    private val question: Question) : Serializable
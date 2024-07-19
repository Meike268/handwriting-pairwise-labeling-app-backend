package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.*

@Entity
class Task(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var taskId: Long? = null,

    @Column
    @ManyToOne
    var sample: Sample? = null,

    @Column
    @ManyToOne
    var question: Question? = null,
)
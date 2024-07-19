package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.*

@Entity
class Answer(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var answerId: Long? = null,

    @Column
    var score: Int? = null,

    @Column
    @ManyToOne
    var task: Task? = null,

    @Column
    @ManyToOne
    var user: User? = null,
)
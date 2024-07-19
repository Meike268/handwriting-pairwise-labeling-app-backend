package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.*

@Entity
class Question(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var questionId: Long? = null,

    @Column
    var instructionText: String? = null
)
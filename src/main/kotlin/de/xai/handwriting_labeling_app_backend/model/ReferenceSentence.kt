package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity
class ReferenceSentence(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var referenceSentenceId: Long? = null,

    @Column
    var content: String? = null,

    @Column
    @ManyToMany
    var possibleQuestions: List<Question>? = null
)
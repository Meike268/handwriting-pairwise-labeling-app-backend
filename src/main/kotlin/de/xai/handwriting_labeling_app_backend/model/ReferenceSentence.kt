package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany

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
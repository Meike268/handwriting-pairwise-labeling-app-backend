package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.*

@Entity
class Example(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var exampleId: Long? = null,

    @Column
    var positiveExampleImagePath: String? = null,

    @Column
    var negativeExampleImagePath: String? = null,

    @Column
    @ManyToOne
    var question: Question? = null,

    @Column
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null
)
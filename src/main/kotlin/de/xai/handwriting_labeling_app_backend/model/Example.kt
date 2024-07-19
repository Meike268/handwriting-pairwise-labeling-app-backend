package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(ExampleId::class)
class Example(

    @Column
    var positiveExampleImagePath: String? = null,

    @Column
    var negativeExampleImagePath: String? = null,

    @Column
    @Id
    @ManyToOne
    var question: Question? = null,

    @Column
    @Id
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null
)

class ExampleId(
    private val question: Question,
    private val referenceSentence: ReferenceSentence) : Serializable
package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "example_pair")
@IdClass(ExamplePairId::class)
class ExamplePair(

    @Column(name = "positive_example_image_path")
    var positiveExampleImagePath: String? = null,

    @Column(name = "negative_example_image_path")
    var negativeExampleImagePath: String? = null,

    @JoinColumn(name = "reference_sentence_id")
    @Id
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null,

    @JoinColumn(name = "question_id")
    @Id
    @ManyToOne
    var question: Question? = null
)

class ExamplePairId(
    private val question: Question,
    private val referenceSentence: ReferenceSentence
) : Serializable
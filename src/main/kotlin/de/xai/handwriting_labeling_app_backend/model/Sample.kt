package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import jakarta.persistence.*

@Entity
@IdClass(SampleId::class)
class Sample(

    @Column
    @Id
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null,

    @Id @Column
    var studentId: Int? = null,

    @Column
    var sentenceImagePath: String? = null,
)


class SampleId(
    private val studentId: Int,
    private val referenceSentence: ReferenceSentence) : Serializable


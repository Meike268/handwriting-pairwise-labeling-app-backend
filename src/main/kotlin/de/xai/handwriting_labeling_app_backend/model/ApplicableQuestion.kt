package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "applicable_question")
@IdClass(ApplicableQuestionId::class)
class ApplicableQuestion(

    @JoinColumn(name = "reference_sentence_id")
    @Id
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null,

    @JoinColumn(name = "question_id")
    @Id
    @ManyToOne
    var question: Question? = null
)

data class ApplicableQuestionId(
    private val referenceSentence: ReferenceSentence? = null,
    private val question: Question? = null
) : Serializable
package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.ReferenceSentence

data class ReferenceSentenceInfoBody(
    val id: Long,
    val content: String,
) {
    companion object {
        fun fromReferenceSentence(referenceSentence: ReferenceSentence): ReferenceSentenceInfoBody {
            return ReferenceSentenceInfoBody(
                id = referenceSentence.id!!,
                content = referenceSentence.content!!
            )
        }
    }
}
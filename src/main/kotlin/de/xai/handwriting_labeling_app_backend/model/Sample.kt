package de.xai.handwriting_labeling_app_backend.model

data class Sample(
    val id: Long,
    val studentId: Long,
    val referenceSentence: ReferenceSentence?
)
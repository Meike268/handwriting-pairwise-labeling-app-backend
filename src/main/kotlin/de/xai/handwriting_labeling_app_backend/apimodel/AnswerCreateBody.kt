package de.xai.handwriting_labeling_app_backend.apimodel

data class AnswerCreateBody(
    val sampleId: Long,
    val questionId: Long,
    val score: Int,
)

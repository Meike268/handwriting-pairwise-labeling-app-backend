package de.xai.handwriting_labeling_app_backend.apimodel

data class AnswerCreateBody(
    val sampleId1: Long,
    val sampleId2: Long,
    val questionId: Long,
    val score: Int,
    val submissionTimestamp: Long,
)

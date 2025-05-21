package de.xai.handwriting_labeling_app_backend.apimodel

data class ReportCreateBody(
    val sampleId1: Long?,
    val sampleId2: Long?,
    val questionId: Long?,
    val message: String,
    val submissionTimestamp: Long,
)
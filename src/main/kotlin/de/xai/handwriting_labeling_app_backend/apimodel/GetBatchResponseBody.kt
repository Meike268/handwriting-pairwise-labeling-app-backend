package de.xai.handwriting_labeling_app_backend.apimodel

data class GetBatchResponseBody(
    val state: String,
    val body: TaskBatchInfoBody?
)


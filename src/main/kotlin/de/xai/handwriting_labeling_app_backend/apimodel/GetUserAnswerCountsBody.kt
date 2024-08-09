package de.xai.handwriting_labeling_app_backend.apimodel

data class GetUserAnswerCountsBody(
    val submittedAnswersCount: Int,
    var pendingAnswersCount: Int?
)

package de.xai.handwriting_labeling_app_backend.apimodel

data class UserCreateBody(
    val username: String,
    val password: String,
    val roleNames: List<String>,
)

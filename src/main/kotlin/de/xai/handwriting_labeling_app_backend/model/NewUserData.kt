package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable

class NewUserData(
    val username: String? = null,
    val password: String? = null,
    val role: String? = null,
): Serializable {
}
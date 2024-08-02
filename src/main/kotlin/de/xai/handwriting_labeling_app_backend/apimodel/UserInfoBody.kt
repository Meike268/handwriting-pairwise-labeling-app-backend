package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.ROLE_PREFIX

data class UserInfoBody(
    val username: String,
    val roleNames: List<String>
) {
    companion object {
        fun fromUser(user: User): UserInfoBody {
            return UserInfoBody(
                username = user.username,
                roleNames = user.roles.mapNotNull { role ->
                    role.name?.removePrefix(ROLE_PREFIX)
                }
            )
        }
    }
}


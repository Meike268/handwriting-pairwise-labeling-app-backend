package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserBatchLog
import de.xai.handwriting_labeling_app_backend.repository.UserBatchLogRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import com.fasterxml.jackson.core.type.TypeReference


@Service
class UserBatchLogService(
    private val batchRepository: UserBatchLogRepository,
    private val userRepository: UserRepository
) {
    fun countBatchForUser (
        username: String
    ): Int {

        val objectMapper = ObjectMapper()

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")

        return batchRepository.countByUserId(user.id!!)
    }

    fun getAllBatchLogs(): List<UserBatchLog> {
        return batchRepository.findAll()
    }


}

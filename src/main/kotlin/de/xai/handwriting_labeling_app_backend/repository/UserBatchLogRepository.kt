package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import de.xai.handwriting_labeling_app_backend.model.UserBatchLog

interface UserBatchLogRepository : JpaRepository<UserBatchLog, Long> {
    fun countByUserId(userId: Long): Int
}

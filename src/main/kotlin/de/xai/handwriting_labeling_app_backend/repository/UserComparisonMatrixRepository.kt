package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix

interface UserComparisonMatrixRepository : JpaRepository<UserComparisonMatrix, Long> {
    fun findByUserId(userId: Long): UserComparisonMatrix?
}

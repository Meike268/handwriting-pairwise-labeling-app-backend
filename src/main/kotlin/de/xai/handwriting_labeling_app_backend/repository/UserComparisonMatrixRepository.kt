package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserComparisonMatrixRepository : JpaRepository<UserComparisonMatrix, Long> {
    fun findByUser(user: User): UserComparisonMatrix?
}

package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository: JpaRepository<Role, Long> {
    fun findByName(name: String): Role
}
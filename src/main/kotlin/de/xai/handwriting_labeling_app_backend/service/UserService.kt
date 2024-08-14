package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.UserCreateBody
import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.RoleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository, val roleRepository: RoleRepository): UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        return userRepository.findByUsername(username!!)!!
    }

    fun createUserIfNotExist(userCreateBody: UserCreateBody): User? {
        val userWithNameFromDB = userRepository.findByUsername(userCreateBody.username)

        if (userWithNameFromDB == null) {
            return userRepository.save(User(
                username = userCreateBody.username,
                password = userCreateBody.password,
                roles = userCreateBody.roleNames.map { roleName ->
                    roleRepository.findByName("ROLE_${roleName}")
                }.toSet()
            ))
        } else {
            return null
        }
    }
}
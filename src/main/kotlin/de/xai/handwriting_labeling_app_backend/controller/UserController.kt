package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.model.NewUserData
import de.xai.handwriting_labeling_app_backend.model.Role
import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/users")
class UserController(private val userRepository: UserRepository) {
    @PostMapping("/login")
    fun login(principal: Principal): ResponseEntity<out User> {
        val user = userRepository.findByUsername(principal.name)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @GetMapping("/{id}")
    fun getUser(@PathVariable(value = "id") userId: Long): User = userRepository.findById(userId).get()

    @PostMapping("/")
    fun postUser(@RequestBody newUserData: NewUserData): User {
        val newUserRoles = when (newUserData.role) {
            "ROLE_ADMIN" -> setOf(
                Role(1, "ROLE_ADMIN"),
                Role(2, "ROLE_EXPERT"),
                Role(3, "ROLE_USER")
            )
            "ROLE_EXPERT" -> setOf(
                Role(2, "ROLE_EXPERT"),
                Role(3, "ROLE_USER")
                )
            else -> setOf(Role(3, "ROLE_USER"))

        }

        val newUser = User(
            username = newUserData.username,
            password = newUserData.password,
            //roles = newUserRoles
        )

        val createdUser = userRepository.save(newUser)

        return createdUser

    }
}
package de.xai.handwriting_labeling_app_backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import de.xai.handwriting_labeling_app_backend.apimodel.UserCreateBody
import de.xai.handwriting_labeling_app_backend.apimodel.UserInfoBody
import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.RoleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {
    @PostMapping("/login")
    fun login(principal: Principal): ResponseEntity<out UserInfoBody> {
        userRepository.findByUsername(principal.name)?.let { user ->
            return ResponseEntity.ok(UserInfoBody.fromUser(user))
        } ?: return ResponseEntity.badRequest().body(null)

    }

    @GetMapping("/")
    fun getAllUsers(): List<UserInfoBody> {
        return userRepository.findAll().map { user ->
            UserInfoBody.fromUser(user)
        }
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable(value = "id") userId: Long): ResponseEntity<out UserInfoBody> {
        val user = userRepository.findById(userId).get()
        return ResponseEntity.ok(UserInfoBody.fromUser(user))
    }

    @PostMapping("/")
    fun postUser(@RequestBody userCreateBody: UserCreateBody): User {
        return userRepository.save(User(
            username = userCreateBody.username,
            password = userCreateBody.password,
            roles = userCreateBody.roleNames.map { roleName ->
                roleRepository.findByName("ROLE_${roleName}")
            }.toSet()
        ))
    }
}
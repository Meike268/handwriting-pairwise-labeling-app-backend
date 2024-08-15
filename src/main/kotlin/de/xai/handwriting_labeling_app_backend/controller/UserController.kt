package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.UserCreateBody
import de.xai.handwriting_labeling_app_backend.apimodel.UserInfoBody
import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import de.xai.handwriting_labeling_app_backend.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/login")
    fun login(principal: Principal): ResponseEntity<out UserInfoBody> {
        userRepository.findByUsername(principal.name)?.let { user ->
            return ResponseEntity.ok(UserInfoBody.fromUser(user))
        } ?: return ResponseEntity.badRequest().body(null)

    }

    @GetMapping
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

    @PostMapping
    fun postUser(@RequestBody userCreateBody: UserCreateBody): ResponseEntity<User> {
        logger.info("Create new user: $userCreateBody")
        val savedUser = userService.createUserIfNotExist(userCreateBody)

        if (savedUser != null) {
            logger.debug("New user was successfully created: {}", savedUser)
            return ResponseEntity.ok(savedUser)
        } else {
            logger.debug("Could not create new user. Maybe name already exists.")
            return ResponseEntity.status(409).body(null)
        }
    }
}
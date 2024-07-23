package de.xai.handwriting_labeling_app_backend.controller

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
    fun postUser(@RequestBody user: User): User {
        return userRepository.save(user)
    }
}
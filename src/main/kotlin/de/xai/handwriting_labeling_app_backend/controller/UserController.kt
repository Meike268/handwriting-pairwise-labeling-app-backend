package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(private val userRepository: UserRepository) {

    @GetMapping("/users")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable(value = "id") userId: Long): User = userRepository.findById(userId).get()

    @PostMapping("/users")
    fun postUser(@RequestBody user: User): User {
        return userRepository.save(user)
    }
}
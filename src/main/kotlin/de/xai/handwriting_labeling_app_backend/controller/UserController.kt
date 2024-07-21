package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.web.bind.annotation.*


@RestController
class UserController(private val userRepository: UserRepository) {

    @GetMapping("/api/users")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @GetMapping("/{id}")
    fun getUser(@PathVariable(value = "id") userId: Long): User = userRepository.findById(userId).get()

    @PostMapping("/api/users")
    fun postUser(@RequestBody user: User): User = userRepository.save(user)

}
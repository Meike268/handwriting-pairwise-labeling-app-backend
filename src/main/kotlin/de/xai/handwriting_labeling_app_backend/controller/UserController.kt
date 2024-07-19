package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import de.xai.handwriting_labeling_app_backend.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

//    @GetMapping("/")
//    fun getAllUsers(): List<User> = userRepository.findAll()
//
//    @GetMapping("/{id}")
//    fun getUser(@PathVariable(value = "id") userId: Long): User = userRepository.findById(userId).get()
//
//    @PostMapping("/")
//    fun postUser(@RequestBody user: User): User {
//        return userRepository.save(user)
//    }
}
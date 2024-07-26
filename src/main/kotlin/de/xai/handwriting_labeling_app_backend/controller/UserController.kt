package de.xai.handwriting_labeling_app_backend.controller

import com.fasterxml.jackson.databind.node.ObjectNode
import de.xai.handwriting_labeling_app_backend.model.User
import de.xai.handwriting_labeling_app_backend.repository.RoleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/users")
class UserController(private val userRepository: UserRepository, val roleRepository: RoleRepository) {
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
    fun postUser(@RequestBody userJson: ObjectNode): User {
        return userRepository.save(User(
            username = userJson.get("username").textValue(),
            password = userJson.get("password").textValue(),
            roles = userJson.get("roles").toSet().map{ roleRepository.findByName("ROLE_${it.textValue()}") }.toSet()
        ))
    }
}
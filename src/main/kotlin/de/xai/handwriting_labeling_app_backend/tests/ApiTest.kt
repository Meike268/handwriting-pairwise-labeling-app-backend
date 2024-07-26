package de.xai.handwriting_labeling_app_backend.tests

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
class ApiTest {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<out String> {
        return ResponseEntity.ok("pong")
    }

    @PostMapping("/addOne")
    fun addOne(@RequestBody number: Int): ResponseEntity<out Int> {
        return ResponseEntity.ok(number + 1)
    }

    @PutMapping("/greeting", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun greeting(principal: Principal, @RequestBody greeting: Greeting): ResponseEntity<String> {
        return ResponseEntity.ok("${greeting.clause} ${principal.name}")
    }
}

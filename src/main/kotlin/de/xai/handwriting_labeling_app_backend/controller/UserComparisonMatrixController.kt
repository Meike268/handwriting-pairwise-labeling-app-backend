package de.xai.handwriting_labeling_app_backend.controller


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import de.xai.handwriting_labeling_app_backend.service.UserComparisonMatrixService
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import org.slf4j.LoggerFactory

import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*



@RestController
@RequestMapping("/matrix")
class UserComparisonMatrixController(
    private val matrixService: UserComparisonMatrixService,
    private val matrixRepo: UserComparisonMatrixRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Endpoint to fetch all user comparison matrices
    @GetMapping
    fun getAllMatrices(): ResponseEntity<List<UserComparisonMatrix>> {
        logger.info("Getting all matrices.")

        val matrices = matrixService.getAllMatrices()
        return ResponseEntity.ok(matrices)
    }

    // Endpoint to fetch matrix for a specific user
    @GetMapping("/{username}")
    fun getMatrix(@PathVariable username: String): ResponseEntity<Pair<Array<IntArray>, List<Long>>> {
        logger.info("Received new request for user ${username}")

        return ResponseEntity.ok(
            matrixService.getMatrixForUser(username)
        )

    }


    // Endpoint to record a comparison between winner and loser
    @PostMapping("/{username}/comparison")
    fun recordComparison(
        @PathVariable username: String,
        @RequestParam winnerId: Long,
        @RequestParam loserId: Long
    ): ResponseEntity<Void> {
        try {
            matrixService.recordComparison(username, winnerId, loserId)
            return ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // Endpoint to save a matrix for a user
    @PostMapping("/{username}/save")
    fun saveMatrix(
        @PathVariable username: String,
        @RequestBody matrix: Array<IntArray>,
        @RequestBody samples: List<Long>
    ): ResponseEntity<Void> {
        try {
            matrixService.saveMatrixForUser(username, matrix, samples)
            return ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }
}

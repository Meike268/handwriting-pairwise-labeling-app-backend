package de.xai.handwriting_labeling_app_backend.controller


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import de.xai.handwriting_labeling_app_backend.service.UserBatchLogService
import de.xai.handwriting_labeling_app_backend.repository.UserBatchLogRepository
import de.xai.handwriting_labeling_app_backend.model.UserBatchLog
import org.slf4j.LoggerFactory

import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*



@RestController
@RequestMapping("/userBatchLog")
class UserBatchLogController(
    private val batchService: UserBatchLogService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Endpoint to fetch all user batch logs
    @GetMapping
    fun getAllBatchLogs(): ResponseEntity<List<UserBatchLog>> {
        logger.info("Getting all batch logs.")

        val logs = batchService.getAllBatchLogs()
        return ResponseEntity.ok(logs)
    }

    // Endpoint to get batch count for a specific user
    @GetMapping("/{username}")
    fun getCountBatchForUser(@PathVariable username: String): ResponseEntity<Int> {
        logger.info("Received new request for user $username")

        return ResponseEntity.ok(batchService.countBatchForUser(username))
    }


}

package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.ReportCreateBody
import de.xai.handwriting_labeling_app_backend.model.Report
import de.xai.handwriting_labeling_app_backend.repository.ReportRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/reports")
class ReportController(
    val reportRepository: ReportRepository,
    val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getReports(): List<Report> = reportRepository.findAll()

    @PostMapping
    fun createReport(principal: Principal, @RequestBody body: ReportCreateBody): ResponseEntity<Report> {
        val savedReport = userRepository.findByUsername(principal.name)?.let { user ->
            val report = Report(
                userId = user.id,
                sampleId = body.sampleId,
                questionId = body.questionId,
                message = body.message,
                submissionTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(body.submissionTimestamp), TimeZone.getDefault().toZoneId())
            )

            reportRepository.save(report)
        }

        if (savedReport != null) {
            logger.info("Stored report $savedReport")
        } else {
            logger.error("Failed to store report $body")
        }

        return ResponseEntity.ok(savedReport)
    }

}
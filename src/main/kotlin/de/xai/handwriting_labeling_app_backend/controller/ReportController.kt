package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.model.Report
import de.xai.handwriting_labeling_app_backend.repository.ReportRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reports")
class ReportController(
    val reportRepository: ReportRepository
) {

    @GetMapping
    fun getReports() = reportRepository.findAll()

    @PostMapping
    fun createReport(@RequestBody report: Report) {
        reportRepository.save(report)
    }

}
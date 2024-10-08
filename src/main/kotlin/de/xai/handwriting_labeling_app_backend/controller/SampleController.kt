package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.SampleInfoBody
import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.repository.ReportRepository
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/samples")
class SampleController(
    private val sampleRepository: SampleRepository,
    private val reportRepository: ReportRepository,
) {
    @GetMapping
    fun getAll(): List<SampleInfoBody> {
        val reports = reportRepository.findAll()
        return sampleRepository.findAll()
            .map { sample: Sample ->
                val associatedReports = reports.filter { it.sampleId == sample.id }
                SampleInfoBody.fromSample(sample, associatedReports)
            }
    }
}
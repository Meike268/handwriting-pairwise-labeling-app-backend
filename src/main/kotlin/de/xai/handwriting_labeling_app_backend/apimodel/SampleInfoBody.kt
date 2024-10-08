package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.Report
import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository


data class SampleInfoBody(
    val id: Long,
    val resourceUrl: String,
    val referenceSentenceId: Long?,
    val reports: List<ReportInfoBody>?,
) {
    companion object {
        fun fromSample(sample: Sample, reports: List<Report>? = null): SampleInfoBody {
            return SampleInfoBody(
                id = sample.id,
                resourceUrl = SampleRepository.getResourceUrl(sample),
                referenceSentenceId = sample.referenceSentence?.id,
                reports = reports?.map { ReportInfoBody.fromReport(it) }
            )
        }
    }
}
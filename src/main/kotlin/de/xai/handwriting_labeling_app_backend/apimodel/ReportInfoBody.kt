package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.Report
import java.time.LocalDateTime

data class ReportInfoBody (
    val id: Long?,
    val userId: Long?,
    val sampleId: Long?,
    val questionId: Long?,
    val message: String?,
    val submissionTimestamp: LocalDateTime?,
) {
    companion object {
        fun fromReport(report: Report): ReportInfoBody {
            return ReportInfoBody(
                id = report.id,
                userId = report.userId,
                sampleId = report.sampleId,
                questionId = report.questionId,
                message = report.message,
                submissionTimestamp = report.submissionTimestamp
            )
        }
    }
}
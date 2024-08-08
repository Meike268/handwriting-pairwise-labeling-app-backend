package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*

@Entity
@Table(name = "report")
class Report {
    @Column(name = "report_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(name = "user_id")
    val userId: Long? = null

    @Column(name = "date")
    val sampleId: Long? = null

    @Column(name = "message")
    val message: String? = null

    @Column(name = "submission_timestamp")
    val submissionTimestamp: Long? = null
}
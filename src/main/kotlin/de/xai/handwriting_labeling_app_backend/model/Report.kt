package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "report")
class Report (
    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "sample_id")
    val sampleId: Long? = null,

    @Column(name = "question_id")
    val questionId: Long? = null,

    @Column(name = "message")
    val message: String? = null,

    @Column(name = "submission_timestamp")
    val submissionTimestamp: LocalDateTime? = null
)
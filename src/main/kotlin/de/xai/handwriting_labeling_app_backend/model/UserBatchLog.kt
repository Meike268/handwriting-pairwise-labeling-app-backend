package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime


@Entity
data class UserBatchLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,

    val timestamp: LocalDateTime = LocalDateTime.now()
)

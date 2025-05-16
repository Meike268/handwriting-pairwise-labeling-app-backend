package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Entity
@Table(name = "user_comparison_matrix")
class UserComparisonMatrix(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Lob
    @Column(name = "matrix_json", nullable = false)
    var matrixJson: String
)

package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Entity
@Table(name = "comparison_list")
data class ComparisonList(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "sample1_id", nullable = false)
    var sample1Id: Long? = null,

    @Column(name = "sample2_id", nullable = false)
    var sample2Id: Long? = null,

    @Column(name = "annotated", nullable = false)
    var annotated: Boolean = false,
)


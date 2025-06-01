package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


@Entity
@Table(name = "user_comparison_matrix", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id"])])
class UserComparisonMatrix(
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    var user: User? = null,

    @Column(name = "matrix_json", nullable = false)
    var matrixJson: String = "",

    @Column(name = "sample_ids_json")
    var sampleIdsJson: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}



package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*


@Entity
data class User(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var username: String? = null,

    @Column
    var password: String? = null,

    @Column
    var isExpert: Boolean? = null
)

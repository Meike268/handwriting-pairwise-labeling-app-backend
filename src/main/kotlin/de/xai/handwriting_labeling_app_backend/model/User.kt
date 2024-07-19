package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id


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

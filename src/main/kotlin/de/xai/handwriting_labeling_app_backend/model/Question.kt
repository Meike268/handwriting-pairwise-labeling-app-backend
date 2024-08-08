package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*

@Entity
class Question(

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "example_image_name")
    var exampleImageName: String? = null,
)
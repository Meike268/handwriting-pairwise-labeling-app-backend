package de.xai.handwriting_labeling_app_backend.model

import javax.persistence.*

@Entity
class Sample(

    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var sampleId: Long? = null,

    @Column
    var sentenceText: String? = null,

    @Column
    var sentenceImagePath: String? = null,

    @Column
    var studentId: Int? = null,
)
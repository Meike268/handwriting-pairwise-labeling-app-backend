package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*

@Entity
class Sample(

    @Id
    @Column(name = "id")
    var id: Long? = null,

    @JoinColumn(name = "reference_sentence_id", referencedColumnName = "id")
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null,

    )
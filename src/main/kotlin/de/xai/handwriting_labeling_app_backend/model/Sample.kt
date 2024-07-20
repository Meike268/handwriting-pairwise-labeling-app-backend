package de.xai.handwriting_labeling_app_backend.model

import java.io.Serializable
import jakarta.persistence.*

@Entity
class Sample(

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    var id: Long? = null,

    @JoinColumn(name = "sentence_id", referencedColumnName = "id")
    @ManyToOne
    var referenceSentence: ReferenceSentence? = null,

    @Column
    var studentId: Int? = null,

    @Column
    var sentenceImagePath: String? = null,
)
package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*

@Entity
@Table(name = "reference_sentence")
class ReferenceSentence(

    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "content")
    var content: String? = null,

    @Column
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "applicable_question",
        joinColumns = [JoinColumn(name = "reference_sentence_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "question_id", referencedColumnName = "id")]
    )
    var applicableQuestions: Set<Question>? = null
)
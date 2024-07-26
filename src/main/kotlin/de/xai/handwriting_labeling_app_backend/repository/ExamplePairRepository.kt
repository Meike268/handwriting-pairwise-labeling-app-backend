package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.ExamplePair
import de.xai.handwriting_labeling_app_backend.model.Question
import de.xai.handwriting_labeling_app_backend.model.ReferenceSentence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExamplePairRepository: JpaRepository<ExamplePair, Long> {
    fun findByReferenceSentenceAndQuestion(referenceSentence: ReferenceSentence, question: Question): ExamplePair? {
        return findByReferenceSentenceAndQuestion(referenceSentence, question)
    }
}
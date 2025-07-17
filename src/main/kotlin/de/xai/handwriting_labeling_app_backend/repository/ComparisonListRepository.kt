package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.ComparisonList
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


@Repository
interface ComparisonListRepository: JpaRepository<ComparisonList, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE ComparisonList c SET c.annotated = :annotatedValue WHERE c.sample1Id = :sample1Id AND c.sample2Id = :sample2Id")
    fun updateAnnotatedBySampleIds(sample1Id: Long, sample2Id: Long, annotatedValue: Boolean)

    fun findByAnnotatedFalse(): List<ComparisonList>
}
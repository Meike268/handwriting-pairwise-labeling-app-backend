package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import com.fasterxml.jackson.core.type.TypeReference

import java.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import java.io.ByteArrayInputStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

@Service
class ComparisonListService(
    private val comparisonListRepository: ComparisonListRepository,
    private val answerRepository: AnswerRepository
) {
    fun unannotateOrphanedComparisons(): Int {
        val annotatedComparisons = comparisonListRepository.findByAnnotatedTrue()
        var updatedCount = 0

        annotatedComparisons.forEach { comparison ->
            // Use existsBySamplePair to check if an answer exists for this sample pair
            val exists = answerRepository.existsBySamplePair(
                comparison.sample1Id!!,
                comparison.sample2Id!!
            )
            // If no answer exists, set annotated to false and save
            if (!exists) {
                comparison.annotated = false
                comparisonListRepository.save(comparison)  // Save the updated comparison list entry
                updatedCount++
            }
        }

        return updatedCount
    }

    // Fetch all comparisons
    fun getAllComparisons(): List<ComparisonList> {
        return comparisonListRepository.findAll()
    }

    // Fetch comparisons based on annotation status
    fun getComparisonsByAnnotationStatus(annotated: Boolean): List<ComparisonList> {
        return if (annotated) {
            comparisonListRepository.findByAnnotatedTrue()
        } else {
            comparisonListRepository.findByAnnotatedFalse()
        }
    }

    // Set all unannotated comparisons to annotated=true if a corresponding answer exists
    fun annotateComparisonsWithAnswers(): Int {
        // Fetch all unannotated comparisons
        val unannotatedComparisons = comparisonListRepository.findByAnnotatedFalse()
        var updatedCount = 0

        unannotatedComparisons.forEach { comparison ->
            // Check if an answer exists for this comparison
            val exists = answerRepository.existsBySamplePair(
                comparison.sample1Id!!,
                comparison.sample2Id!!
            )
            // If an answer exists, set the comparison as annotated
            if (exists) {
                comparison.annotated = true
                comparisonListRepository.save(comparison)
                updatedCount++
            }
        }

        return updatedCount
    }
}

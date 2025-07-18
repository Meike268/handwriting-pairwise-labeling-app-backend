package de.xai.handwriting_labeling_app_backend.controller


import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import de.xai.handwriting_labeling_app_backend.service.ComparisonListService
import de.xai.handwriting_labeling_app_backend.repository.ComparisonListRepository
import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.model.ComparisonList
import org.slf4j.LoggerFactory

import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/comparisons")
class ComparisonListController(
    private val comparisonListService: ComparisonListService
) {
    @PostMapping("/cleanOrphans")
    fun cleanOrphanedComparisons(): ResponseEntity<Map<String, Any>> {
        val updated = comparisonListService.unannotateOrphanedComparisons()
        return ResponseEntity.ok(mapOf("updated" to updated))
    }

    // Fetch all comparisons
    @GetMapping
    fun getAllComparisons(): ResponseEntity<List<ComparisonList>> {
        val comparisons = comparisonListService.getAllComparisons()
        return ResponseEntity.ok(comparisons)
    }

    // Fetch comparisons based on annotation status
    @GetMapping("/byAnnotationStatus")
    fun getComparisonsByAnnotationStatus(
        @RequestParam annotated: Boolean? = null
    ): ResponseEntity<List<ComparisonList>> {
        val comparisons = if (annotated != null) {
            comparisonListService.getComparisonsByAnnotationStatus(annotated)
        } else {
            comparisonListService.getAllComparisons()
        }
        return ResponseEntity.ok(comparisons)
    }

    // Endpoint to set all unannotated comparisons to annotated=true if an answer exists
    @PostMapping("/annotate")
    fun annotateComparisons(): ResponseEntity<Map<String, Any>> {
        val updated = comparisonListService.annotateComparisonsWithAnswers()
        return ResponseEntity.ok(mapOf("updated" to updated))
    }
}

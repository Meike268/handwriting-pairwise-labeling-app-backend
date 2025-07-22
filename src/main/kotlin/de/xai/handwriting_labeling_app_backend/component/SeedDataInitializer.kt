package de.xai.handwriting_labeling_app_backend.component

import org.springframework.stereotype.Component
import java.io.BufferedReader
import jakarta.annotation.PostConstruct

import de.xai.handwriting_labeling_app_backend.repository.ComparisonListRepository
import de.xai.handwriting_labeling_app_backend.model.ComparisonList
import org.slf4j.LoggerFactory

@Component
class SeedDataInitializer(
    private val comparisonListRepository: ComparisonListRepository
) {

    @PostConstruct
    fun init() {
        // Only seed if table is empty
        if (comparisonListRepository.count() > 0) return

        val seedData: List<ComparisonList> = javaClass
            .getResourceAsStream("/samples_all_pairs.csv")
            ?.bufferedReader()
            ?.useLines { lines ->
                lines.drop(1) // skip header
                    .map { line ->
                        val tokens = line.split(",")
                        val sample1Id = tokens[0].trim().toLong()
                        val sample2Id = tokens[1].trim().toLong()
                        ComparisonList(
                            sample1Id = sample1Id,
                            sample2Id = sample2Id,
                            annotated = false)
                    }.toList()
            } ?: emptyList()

        comparisonListRepository.saveAll(seedData)
    }
}

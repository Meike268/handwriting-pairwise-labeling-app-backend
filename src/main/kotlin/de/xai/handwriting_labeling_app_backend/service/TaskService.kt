package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Task
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import de.xai.handwriting_labeling_app_backend.repository.ComparisonListRepository
import de.xai.handwriting_labeling_app_backend.model.User
import org.springframework.stereotype.Service

import org.slf4j.LoggerFactory



@Service
class TaskService(
    private val sampleRepository: SampleRepository,
    private val comparisonListRepository: ComparisonListRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findAll(
        username: String,
    ): List<Task> {

        // 1. Get all samples and build a lookup map by ID
        val samplesById = sampleRepository.findAll()
            .filter { it.referenceSentence?.isQuestion1Applicable() == true }
            .associateBy { it.id }

        // 2. Get all comparison pairs where annotated == false
        val comparisonPairs = comparisonListRepository.findByAnnotatedFalse()

        // 3. Filter only valid sample pairs and create tasks
        val tasks = comparisonPairs.mapNotNull { comparison ->
            val sample1 = samplesById[comparison.sample1Id]
            val sample2 = samplesById[comparison.sample2Id]

            // Ensure both samples exist and question 1 is applicable to both
            if (sample1 != null && sample2 != null &&
                sample1.referenceSentence?.isQuestion1Applicable() == true &&
                sample2.referenceSentence?.isQuestion1Applicable() == true
            ) {
                val question = sample1.referenceSentence!!.applicableQuestions
                    ?.firstOrNull { it.id == 1L }

                question?.let { Task(sample1, sample2, it) }
            } else {
                null
            }
        }

        return tasks

        /*
        // get comparison matrix for user from db
        //val (matrix, _) = matrixService.getMatrixForUser(username)

        // get recommended pairsToCompare and meanEIG from asapService based on comparison matrix
        //val (pairsToCompare, meanEIG) = asapService.getPairsToCompare(matrix)

        // only label pairs that haven't been labeled yet
        //val filteredPairs = pairsToCompare.filter { (row, col) ->
        //    matrix[row][col] == 0 && matrix[col][row] == 0
        //}

        logger.info("filteredPairs: $filteredPairs")
        logger.info("meanEIG: $meanEIG")

        return filteredPairs.flatMap { (i, j) ->
            val sample1 = samples[i]
            val sample2 = samples[j]

            // Check if Question ID 1 is applicable to both sample1's and sample2's reference sentence
            if (sample1.referenceSentence?.isQuestion1Applicable() == true && sample2.referenceSentence?.isQuestion1Applicable() == true) {

                // If Question ID 1 is applicable to both samples, create tasks for question 1
                sample1.referenceSentence!!.applicableQuestions
                    ?.firstOrNull { it.id == 1L }
                    ?.let { question -> listOf(Task(sample1, sample2, question)) }
                    ?: emptyList<Task>()

            } else {
                // If Question ID 1 is not applicable to either sample, return an empty list
                emptyList<Task>()
            }
        }

        */

    }
}



/**
Current functionality:
 * Goes over all samples
 * Checks if sample has associated referenceSentence
 * Checks if associated referenceSentence has applicable Questions
 * Creates a new task for each question and the associated sample -> Task(sample, question)

Adjustments:
* Each task should contain two randomly paired samples (based on some sorting algorithm --> ToDo)
* We need to decide on a subset of samples to be annotated by specific annotators and those should be excluded to the others
 * */
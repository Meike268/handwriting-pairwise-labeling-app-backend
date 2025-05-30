package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Task
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import de.xai.handwriting_labeling_app_backend.service.AsapService
import de.xai.handwriting_labeling_app_backend.model.User
import org.springframework.stereotype.Service

import org.slf4j.LoggerFactory



@Service
class TaskService(
    private val sampleRepository: SampleRepository,
    private val asapService: AsapService,
    private val matrixService: UserComparisonMatrixService,
    private val matrixRepository: UserComparisonMatrixRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun findAll(
        username: String,
    ): List<Task> {

        // get all samples sorted by ID and filtered by applicability of question 1
        val samples = sampleRepository.findAll()
            .sortedBy { it.id }
            .filter { it.referenceSentence?.isQuestion1Applicable() == true }

        // get comparison matrix for user from db
        val (matrix, _) = matrixService.getMatrixForUser(username)

        // get recommended pairsToCompare and maxEIG from asapService based on comparison matrix
        val (pairsToCompare, maxEIG) = asapService.getPairsToCompare(matrix)

        logger.info("pairsToCompare: $pairsToCompare")
        logger.info("maxEIG: $maxEIG")

        if(maxEIG < 0.5) {
            // All comparisons offer low additional value -> stop requesting more
            return emptyList<Task>()
        }

        return pairsToCompare.flatMap { (i, j) ->
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
package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Task
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixServiceRepository
import org.springframework.stereotype.Service


@Service
class TaskService(
    private val sampleRepository: SampleRepository,
    private val asapClient: AsapClient,
    private val matrixService: UserComparisonMatrixService
) {
    fun findAll(user: User): List<Task> {
        val samples = sampleRepository.findAll()
            .sortedBy { it.id }
            .filter { it.referenceSentence?.isQuestion1Applicable() == true } // TODO: change to only question 1

        val matrix = matrixService.getMatrixForUser(user, samples.size)

        val pairsToCompare = asapClient.getPairsToCompare(matrix)

        if(maxEIG < 0.5) {
            // All comparisons are low-value - stop requesting more
            return emptyList() // TODO: tell frontend
        }

        return pairsToCompare.flatMap { (i, j) ->
            val sample1 = samples[i]
            val sample2 = samples[j]

            // Check if Question ID 1 is applicable to both sample1's and sample2's reference sentence
            if (sample1.referenceSentence?.isQuestion1Applicable() == true && sample2.referenceSentence?.isQuestion1Applicable() == true) {
                // If Question ID 1 is applicable to both samples, create tasks for applicable questions
                sample1.referenceSentence!!.applicableQuestions.map { question ->
                    Task(sample1, sample2, question)
                }
            } else {
                // If Question ID 1 is not applicable to either sample, return an empty list (or handle differently)
                emptyList() // TODO: tell frontend
            }
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
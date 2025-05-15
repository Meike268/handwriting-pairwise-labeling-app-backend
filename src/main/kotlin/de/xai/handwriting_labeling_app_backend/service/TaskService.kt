package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Task
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import org.springframework.stereotype.Service

@Service
class TaskService(
    sampleRepository: SampleRepository,
) {
    private val tasks: List<Task> = sampleRepository.findAll().flatMap { sample -> sample.referenceSentence?.applicableQuestions?.map { question -> Task(sample, question) } ?: listOf() }

    fun findAll(): List<Task> = tasks
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
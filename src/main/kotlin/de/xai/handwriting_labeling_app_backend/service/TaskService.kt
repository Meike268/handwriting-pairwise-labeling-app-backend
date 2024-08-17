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
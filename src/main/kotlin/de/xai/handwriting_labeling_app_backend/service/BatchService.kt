package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.SampleInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.TaskBatchInfoBody
import de.xai.handwriting_labeling_app_backend.repository.QuestionRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BatchService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val sampleService: SampleService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateRandomBatch(username: String) : TaskBatchInfoBody {
        val user = userRepository.findByUsername(username)
        logger.info("Generating random batch for user $user")

        return TaskBatchInfoBody(
            question = questionRepository.findAll()[0],
            samples = sampleService.findAll().map { SampleInfoBody.fromSample(it) }.toList()
        )
    }
}
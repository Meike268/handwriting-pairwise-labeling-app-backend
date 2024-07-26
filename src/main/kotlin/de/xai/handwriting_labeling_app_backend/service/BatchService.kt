package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.apimodel.ExamplePairInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.ReferenceSentenceInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.SampleInfoBody
import de.xai.handwriting_labeling_app_backend.apimodel.TaskBatchInfoBody
import de.xai.handwriting_labeling_app_backend.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BatchService(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val sampleRepository: SampleRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository,
    private val examplePairRepository: ExamplePairRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateRandomBatch(username: String) : TaskBatchInfoBody {
        val user = userRepository.findByUsername(username)
        logger.info("Generating random batch for user $user")
        val question = questionRepository.findAll()[0]
        val referenceSentence = referenceSentenceRepository.findById(1).get()
        val examplePair = examplePairRepository.findByReferenceSentenceAndQuestion(referenceSentence, question)
        val samples = sampleRepository.findAll().map { SampleInfoBody.fromSample(it) }.toList()

        return TaskBatchInfoBody(
            question = question,
            referenceSentence = ReferenceSentenceInfoBody.fromReferenceSentence(referenceSentence),
            examplePair = ExamplePairInfoBody.fromExamplePair( examplePair!! ),
            samples = samples
        )
    }
}
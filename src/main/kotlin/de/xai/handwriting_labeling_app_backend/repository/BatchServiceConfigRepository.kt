package de.xai.handwriting_labeling_app_backend.repository

import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.BatchServiceConfig
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.batchServiceConfigFile
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class BatchServiceConfigRepository(
    val referenceSentenceRepository: ReferenceSentenceRepository,
    val questionRepository: QuestionRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getConfig(): BatchServiceConfig {

        val configJsonString: String = batchServiceConfigFile.readText(Charsets.UTF_8)
        val config = ObjectMapper().readValue(configJsonString, BatchServiceConfig::class.java)

        validateSentencePrio(config)
        validateQuestionPrio(config)

        return config
    }


    private fun validateSentencePrio(config: BatchServiceConfig) {
        // validate that config and db are in sync
        var totalSentencePrioPercentage = 0
        for (sentencePrio in config.prioritizedReferenceSentences) {
            try {
                referenceSentenceRepository.findById(sentencePrio.referenceSentencesId).get()
                totalSentencePrioPercentage += sentencePrio.priorityPercentage
            } catch (e: NoSuchElementException) {
                logger.error("BatchServiceConfig not in sync with reference sentences in db./n${e.message}")
            }
        }
        assert(totalSentencePrioPercentage == 100)
    }

    private fun validateQuestionPrio(config: BatchServiceConfig) {
        for (questionPrio in config.prioritizedQuestions) {
            try {
                questionRepository.findById(questionPrio.questionId).get()
            } catch (e: NoSuchElementException) {
                logger.error("BatchServiceConfig not in sync with questions in db./n${e.message}")
            }
        }
    }
}
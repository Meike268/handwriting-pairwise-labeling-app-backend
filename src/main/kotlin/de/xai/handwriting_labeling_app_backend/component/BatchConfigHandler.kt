package de.xai.handwriting_labeling_app_backend.component

import de.xai.handwriting_labeling_app_backend.model.BatchServiceConfig
import de.xai.handwriting_labeling_app_backend.repository.QuestionRepository
import de.xai.handwriting_labeling_app_backend.repository.ReferenceSentenceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BatchConfigHandler(
    private val questionRepository: QuestionRepository,
    private val referenceSentenceRepository: ReferenceSentenceRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun readBatchServiceConfig(): BatchServiceConfig {
        val config = BatchServiceConfig.fromFile()

        validateSentencePrioAgainstDB(config, referenceSentenceRepository)
        validateQuestionPrio(config, questionRepository)

        return config
    }

    fun writeBatchServiceConfig(config: BatchServiceConfig): BatchServiceConfig {
        validateSentencePrioAgainstDB(config, referenceSentenceRepository)
        validateQuestionPrio(config, questionRepository)

        BatchServiceConfig.toFile(config)

        return readBatchServiceConfig()
    }

    private fun validateSentencePrioAgainstDB(
        config: BatchServiceConfig,
        referenceSentenceRepository: ReferenceSentenceRepository
    ) {
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

    private fun validateQuestionPrio(config: BatchServiceConfig, questionRepository: QuestionRepository) {
        for (questionPrio in config.prioritizedQuestions) {
            try {
                questionRepository.findById(questionPrio.questionId).get()
            } catch (e: NoSuchElementException) {
                logger.error("BatchServiceConfig not in sync with questions in db./n${e.message}")
            }
        }
    }
}

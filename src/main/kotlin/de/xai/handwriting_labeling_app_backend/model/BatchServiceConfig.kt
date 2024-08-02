package de.xai.handwriting_labeling_app_backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.batchServiceConfigFile
import java.io.Serializable

data class BatchServiceConfig(
    @JsonProperty("samplesOrigin")
    val samplesOrigin: String,

    @JsonProperty("batchSize")
    val batchSize: Int,

    /**
     * For each applicable combination of sample and question, we need to collect n answers by experts
     * */
    @JsonProperty("minExpertAnswerCount")
    val minExpertAnswerCount: Int,

    /**
     * For each applicable combination of sample and question, we need to collect n answers by any user
     * */
    @JsonProperty("targetAnswerCount")
    val targetAnswerCount: Int,

    @JsonProperty("prioritizedReferenceSentences")
    val prioritizedReferenceSentences: List<PrioritizedReferenceSentence>,

    @JsonProperty("prioritizedQuestions")
    val prioritizedQuestions: List<PrioritizedQuestion>
) : Serializable {
    companion object {

        fun fromFile(): BatchServiceConfig {
            return batchServiceConfigFile.readText(Charsets.UTF_8).let { configString ->
                ObjectMapper().readValue(configString, BatchServiceConfig::class.java)
            }
        }

        fun toFile(config: BatchServiceConfig) {
            batchServiceConfigFile.writeText(
                ObjectMapper().writeValueAsString(config),
                Charsets.UTF_8
            )
        }
    }
}

data class PrioritizedReferenceSentence(
    @JsonProperty("referenceSentencesId")
    val referenceSentencesId: Long,
    @JsonProperty("priority")
    val priority: Int
) : Serializable

data class PrioritizedQuestion(
    @JsonProperty("questionId")
    val questionId: Long,

    // 1 = highest priority, 2 = second highest priority, ...
    @JsonProperty("priority")
    val priority: Int

) : Serializable


package de.xai.handwriting_labeling_app_backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class BatchServiceConfig(
    @JsonProperty("samplesOrigin")
    var samplesOrigin: String,

    @JsonProperty("prioritizedReferenceSentences")
    var prioritizedReferenceSentences: List<PrioritizedReferenceSentence>,

    @JsonProperty("prioritizedQuestions")
    var prioritizedQuestions: List<PrioritizedQuestion>
) : Serializable

data class PrioritizedReferenceSentence(
    @JsonProperty("referenceSentencesId")
    val referenceSentencesId: Long,
    @JsonProperty("priorityPercentage")
    val priorityPercentage: Int
) : Serializable

data class PrioritizedQuestion(
    @JsonProperty("questionId")
    val questionId: Long,

    // 1 = highest priority, 2 = second highest priority, ...
    @JsonProperty("priorityPercentage")
    val priority: Int
) : Serializable
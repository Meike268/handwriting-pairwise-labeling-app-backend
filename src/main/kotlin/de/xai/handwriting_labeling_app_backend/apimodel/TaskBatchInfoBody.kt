package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.Example
import de.xai.handwriting_labeling_app_backend.model.Question

data class TaskBatchInfoBody(
    val userAnswerCounts: GetUserAnswerCountsBody,
    val question: Question,
    val example: Example,
    val referenceSentence1: ReferenceSentenceInfoBody,
    val referenceSentence2: ReferenceSentenceInfoBody,
    val samplePairs: List<Pair<SampleInfoBody, SampleInfoBody>>
)
package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.Question

data class TaskBatchInfoBody(
    val question: Question,
    val referenceSentence: ReferenceSentenceInfoBody,
    val examplePair: ExamplePairInfoBody,
    val samples: List<SampleInfoBody>,
)
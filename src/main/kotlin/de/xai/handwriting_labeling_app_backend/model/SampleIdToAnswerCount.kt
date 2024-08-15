package de.xai.handwriting_labeling_app_backend.model

/**
 * Interface used to map response of group by query to entities
 * */
interface SampleIdToAnswerCount{
    val sampleId: Long?
    val answerCount: Int?}
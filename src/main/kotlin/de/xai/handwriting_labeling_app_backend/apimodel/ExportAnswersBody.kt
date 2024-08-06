package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.ReferenceSentence

data class ExportAnswersBody(
    val metaData: ExportAnswersMetaData,
    val answers: List<ExportAnswerInfoBody>
)

data class ExportAnswersMetaData(
    val questionIds: List<Long>,
    // reference sentence also holds applicableQuestions relationship
    val referenceSentences: List<ReferenceSentence>,
    val samplesDirectoryName: String,
    val samplesCount: Int,
)

data class ExportAnswerInfoBody(
    val userId: Long,
    val sampleId: Long,
    val referenceSentenceId: Long,
    val questionId: Long,
    val score: Int,
    val submissionTimestamp: String
)
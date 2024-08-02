package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.ReferenceSentence

data class ExportAnswersBody(
    val xaiMetaData: XAiExportAnswersMetaData?,
    val xaiAnswers: List<XAiExportAnswerInfoBody>?,
    val othersAnswers: List<OthersExportAnswersBody>?
)

data class XAiExportAnswersMetaData(
    val questionIds: List<Long>,
    // reference sentence also holds applicableQuestions relationship
    val referenceSentences: List<ReferenceSentence>,
    val samplesDirectoryName: String,
    val samplesCount: Int,
)

data class XAiExportAnswerInfoBody(
    val userId: Long,
    val sampleId: Long,
    val referenceSentenceId: Long,
    val questionId: Long,
    val score: Int
)

data class OthersExportAnswersBody(
    val userId: Long,
    val sampleId: Long,
    val score: Int
)
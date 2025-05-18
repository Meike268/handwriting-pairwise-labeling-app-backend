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
    val userInfos: List<UserInfoBody>,
    val samplesDirectoryName: String,
    val samplesCount: Int,
)

data class XAiExportAnswerInfoBody(
    val userId: Long,
    val sampleId1: Long,
    val sampleId2: Long,
    val referenceSentenceId1: Long,
    val referenceSentenceId2: Long,
    val questionId: Long,
    val score: Int,
    val submissionTimestamp: String
)

data class OthersExportAnswersBody(
    val userId: Long,
    val sampleId1: Long,
    val sampleId2: Long,
    val score: Int
)
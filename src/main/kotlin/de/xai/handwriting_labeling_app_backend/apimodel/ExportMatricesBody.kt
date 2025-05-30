package de.xai.handwriting_labeling_app_backend.apimodel

data class ExportMatricesBody(
    val xaiMetaData: XAiExportMatricesMetaData?,
    val xaiMatrices: List<XAiExportMatrixInfoBody>?,
)

data class XAiExportMatricesMetaData(
    val userInfos: List<UserInfoBody>,
)

data class XAiExportMatrixInfoBody(
    val userId: Long,
    val matrix: Array<IntArray>,
    val sampleIds: List<Long>
)


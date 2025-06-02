package de.xai.handwriting_labeling_app_backend.model


data class AsapResponse(
    val pairs: List<List<Int>>,
    val mean_eig: Double
)
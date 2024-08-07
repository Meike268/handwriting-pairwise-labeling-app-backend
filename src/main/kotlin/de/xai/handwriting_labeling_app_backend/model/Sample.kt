package de.xai.handwriting_labeling_app_backend.model

import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import java.io.File

data class Sample(
    val id: Long,
    val referenceSentence: ReferenceSentence?
) {
    fun getResourceFile(): File {
        return SampleRepository.getResourceFile(this)
    }
    fun getResourceUrl(): String {
        return SampleRepository.getResourceUrl(this)
    }
}
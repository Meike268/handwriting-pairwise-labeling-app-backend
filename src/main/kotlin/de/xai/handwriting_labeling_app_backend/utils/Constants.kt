package de.xai.handwriting_labeling_app_backend.utils

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.File

class Constants {
    companion object {
        val samplesDir = File("src/main/resources/public/files/images/samples")
        val xaiSentencesDirectoryName = "xai_sentences"
        val xaiSentencesDirectory = File("${samplesDir.path}/$xaiSentencesDirectoryName")
        val baseUrl: String
            get() = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val samplesUrl = "$baseUrl/files/images/samples"
    }
}
package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.repository.ReferenceSentenceRepository
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.File

@Service
class SampleService(
    private val referenceSentenceRepository: ReferenceSentenceRepository
) {
    private final val samplesDir = File("src/main/resources/public/files/images/samples")
    private final val xaiSentencesDir = File(samplesDir.path + "/xai_sentences")

    fun fromFile(file: File): Sample {
        val studentId = file.name.split("_")[0].toLong()
        val sampleId = file.name.split("_")[1].replace(".png", "").toLong()
        return if (file.parentFile.parentFile == xaiSentencesDir)
            Sample(sampleId, studentId, referenceSentenceRepository.findById(file.parentFile.name.toLong()).get())
        else
            Sample(sampleId, studentId, null)
    }

    fun findAll(): List<Sample> {
        return samplesDir.walk().mapNotNull { nestedDirectoryOrFile ->
            if (nestedDirectoryOrFile.isDirectory)
                null
            else
                this.fromFile(nestedDirectoryOrFile)
        }.toList()
    }

    companion object {
        fun getResourceUrl(sample: Sample): String {
            val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            val sampleUrl = "$baseUrl/files/images/samples"

            return if (sample.referenceSentence != null) {
                "$sampleUrl/xai_sentences/${sample.referenceSentence.id}/${sample.studentId}_${sample.id}.png"
            } else {
                "$sampleUrl/greenline_de/${sample.studentId}_${sample.id}.png"
            }
        }
    }
}
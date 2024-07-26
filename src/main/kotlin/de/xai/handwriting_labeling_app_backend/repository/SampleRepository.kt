package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Sample
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.File
import java.io.FileNotFoundException

@Repository
class SampleRepository(
    private val referenceSentenceRepository: ReferenceSentenceRepository
) {
    fun fromFile(file: File): Sample {
        if (!file.exists()) {
            throw FileNotFoundException()
        }

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

    fun findById(id: Long): Sample? {
        return findAll().find { it.id == id }
    }

    /**
     * Returns a sample if the corresponding File exists or null if it doesn't.
     */
    fun findByIdAndStudentIdAndReferenceSentenceId(id: Long, studentId: Long, referenceSentenceId: Long?): Sample? {
        return try {
            fromFile(getResourceFile(id, studentId, referenceSentenceId))
        } catch (e: FileNotFoundException) {
            null
        }
    }

    companion object {
        private val samplesDir = File("src/main/resources/public/files/images/samples")
        private val xaiSentencesDir = File(samplesDir.path + "/xai_sentences")

        fun getResourceUrl(id: Long, studentId: Long, referenceSentenceId: Long?): String {
            val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            val sampleUrl = "$baseUrl/files/images/samples"

            return if (referenceSentenceId != null) {
                "$sampleUrl/xai_sentences/${referenceSentenceId}/${studentId}_${id}.png"
            } else {
                "$sampleUrl/others/${studentId}_${id}.png"
            }
        }
        fun getResourceUrl(sample: Sample): String {
            return getResourceUrl(sample.id, sample.studentId, sample.referenceSentence?.id)
        }

        fun getResourceFile(id: Long, studentId: Long, referenceSentenceId: Long?): File {
            return File(if (referenceSentenceId != null) {
                "$xaiSentencesDir/${referenceSentenceId}/${studentId}_${id}.png"
            } else {
                "$samplesDir/others/${studentId}_${id}.png"
            })
        }
        fun getResourceFile(sample: Sample): File {
            return getResourceFile(sample.id, sample.studentId, sample.referenceSentence?.id)
        }
    }
}
package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.samplesDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.samplesUrl
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.xaiSentencesDirectory
import org.springframework.stereotype.Repository
import java.io.File
import java.io.FileNotFoundException

/**
 * This repo does not store samples as entities to the db, but serves as a layer on top of the underlying data folder
 * structure.
 *
 * All sample images are located at resources/public/files/images/samples .
 * The "xai_sentences" directory contains 10 subdirectories, each containing handwriting samples of one reference
 * sentence.
 *
 * resources/public/files/images/samples
 *          - other
 *          - xai_sentences
 *              - 1
 *                  - <student_id>_<unique_sample_id>.png
 *                  - ...
 *              - 2
 *              ...
 *              -10
 * */
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
        return if (file.parentFile.parentFile == xaiSentencesDirectory)
            Sample(sampleId, studentId, referenceSentenceRepository.findById(file.parentFile.name.toLong()).get())
        else
            Sample(sampleId, studentId, null)
    }

    fun findAll(): List<Sample> {
        return samplesDirectory.walk()
            .filter { it.isFile }
            // mac creates .DS_store files in folders that need to be ignored
            .filter { !it.name.startsWith(".") }
            .map { nestedFile ->
                this.fromFile(nestedFile)
            }.toList()
    }

    fun findAllInDirectoryRecursive(directory: File): List<Sample> {
        return directory.walk()
            .filter { it.isFile }
            // mac creates .DS_store files in folders that need to be ignored
            .filter { !it.name.startsWith(".") }
            .map { nestedFile ->
                this.fromFile(nestedFile)
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
        private fun path(id: Long, studentId: Long, referenceSentenceId: Long?): String {
            return if (referenceSentenceId != null) {
                "/$XAI_SENTENCE_DIRECTORY_NAME/${referenceSentenceId}/${studentId}_${id}.png"
            } else {
                "$samplesUrl/others/${studentId}_${id}.png"
            }
        }

        fun getResourceUrl(id: Long, studentId: Long, referenceSentenceId: Long?): String {
            return "$samplesUrl${path(id, studentId, referenceSentenceId)}"
        }

        fun getResourceUrl(sample: Sample): String {
            return getResourceUrl(sample.id, sample.studentId, sample.referenceSentence?.id)
        }

        fun getResourceFile(id: Long, studentId: Long, referenceSentenceId: Long?): File {
            return File("$samplesDirectory${path(id, studentId, referenceSentenceId)}")
        }

        fun getResourceFile(sample: Sample): File {
            return getResourceFile(sample.id, sample.studentId, sample.referenceSentence?.id)
        }
    }
}
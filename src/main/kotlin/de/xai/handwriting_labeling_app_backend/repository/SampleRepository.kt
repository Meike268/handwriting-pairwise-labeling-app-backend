package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Sample
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.OTHERS_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.XAI_SENTENCE_DIRECTORY_NAME
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.samplesDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.SAMPLES_PATH
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
 *                  - <unique_sample_id>.png
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

        val sampleId = file.name.replace(".png", "").toLong()
        return if (file.parentFile.parentFile == xaiSentencesDirectory)
            Sample(sampleId, referenceSentenceRepository.findById(file.parentFile.name.toLong()).get())
        else
            Sample(sampleId, null)
    }

    fun findAll(): List<Sample> {
        return samplesDirectory.walk()
            .filter { it.isFile }
            .map { nestedFile ->
                this.fromFile(nestedFile)
            }.toList()
    }

    fun findAllInDirectoryRecursive(directory: File): List<Sample> {
        return directory.walk()
            .filter { it.isFile }
            .map { nestedFile ->
                this.fromFile(nestedFile)
            }.toList()
    }

    fun findById(id: Long): Sample? {
        return findAll().find { it.id == id }
    }

    companion object {
        private fun path(id: Long, referenceSentenceId: Long?): String {
            return if (referenceSentenceId != null) {
                "/$XAI_SENTENCE_DIRECTORY_NAME/${referenceSentenceId}/${id}.png"
            } else {
                "$SAMPLES_PATH/$OTHERS_DIRECTORY_NAME/${id}.png"
            }
        }

        private fun getResourceUrl(id: Long, referenceSentenceId: Long?): String {
            return "$SAMPLES_PATH${path(id, referenceSentenceId)}"
        }

        fun getResourceUrl(sample: Sample): String {
            return getResourceUrl(sample.id, sample.referenceSentence?.id)
        }

        private fun getResourceFile(id: Long, referenceSentenceId: Long?): File {
            return File("$samplesDirectory${path(id, referenceSentenceId)}")
        }

        fun getResourceFile(sample: Sample): File {
            return getResourceFile(sample.id, sample.referenceSentence?.id)
        }
    }
}
package de.xai.handwriting_labeling_app_backend.repository

import de.xai.handwriting_labeling_app_backend.model.Example
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.examplesDirectory
import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.EXAMPLES_PATH
import org.springframework.stereotype.Repository

@Repository
class ExampleRepository{

    fun findAll(): List<Example> {
        return examplesDirectory.walk()
            .filter { it.isFile }
            .map { nestedFile ->
                Example(
                    exampleImagePath = "$EXAMPLES_PATH/${nestedFile.name}"
                )
            }.toList()
    }

    fun findByImageName(exampleImageName: String): Example? {
        return findAll().find { example -> example.exampleImagePath.endsWith(exampleImageName) }
    }
}
package de.xai.handwriting_labeling_app_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HandwritingLabelingAppBackendApplication

fun main(args: Array<String>) {
	runApplication<HandwritingLabelingAppBackendApplication>(*args)
}
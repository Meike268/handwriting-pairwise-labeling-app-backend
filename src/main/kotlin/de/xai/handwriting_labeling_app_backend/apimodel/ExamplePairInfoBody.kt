package de.xai.handwriting_labeling_app_backend.apimodel

import de.xai.handwriting_labeling_app_backend.model.ExamplePair
import org.springframework.web.servlet.support.ServletUriComponentsBuilder


data class ExamplePairInfoBody(
    val positiveResourceUrl: String,
    val negativeResourceUrl: String,
) {

    // TODO make this not suck so bad by having a good idea on how to structure the ExamplePairs
    companion object {
        fun fromExamplePair(examplePair: ExamplePair): ExamplePairInfoBody {
            val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            val examplesUrl = "$baseUrl/files/images/examples"

            return ExamplePairInfoBody(
                positiveResourceUrl = "${examplesUrl}/${examplePair.positiveExampleImagePath}",
                negativeResourceUrl = "${examplesUrl}/${examplePair.negativeExampleImagePath}"
            )
        }
    }
}
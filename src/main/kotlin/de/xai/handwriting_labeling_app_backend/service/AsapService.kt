package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*

@Service
class AsapService (
    private val asapResponse: AsapResponse
) {
    fun getPairsToCompare(matrix: Array<IntArray>): Pair<List<Pair<Int, Int>>, Double> {
        val pythonScriptPath = "src/main/kotlin/de/xai/handwriting_labeling_app_backend/utils/asap_runner.py"

        val objectMapper = ObjectMapper()

        // serialize the matrix into JSON
        val inputJson = objectMapper.writeValueAsString(mapOf("matrix" to matrix))

        // start the python process
        val process = ProcessBuilder("python", pythonScriptPath)
            .redirectErrorStream(true)
            .start()

        // send the serialized matrix to the Python script's standard input
        process.outputStream.use {
            it.write(inputJson.toByteArray())
            it.flush()
        }

        // read the JSON result from the script's stdout
        val output = process.inputStream.bufferedReader().readText()

        // deserialize the output JSON into an AsapResponse
        val result = objectMapper.readValue(output, AsapResponse::class.java)

        // convert nested list into Kotlin Pair values
        val pairs = result.pairs.map { pair -> Pair(pair[0], pair[1]) }


        return Pair(pairs, result.max_eig)
    }
}
package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory


@Service
class AsapService {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(javaClass)


    fun getPairsToCompare(matrix: Array<IntArray>): Pair<List<Pair<Int, Int>>, Double> {
        val pythonScriptPath = "src/main/kotlin/de/xai/handwriting_labeling_app_backend/utils/asap_runner.py"

        // serialize the matrix into JSON string
        val matrixList = matrix.map { it.toList() } // Convert Array<IntArray> → List<List<Int>>
        val inputJson = objectMapper.writeValueAsString(mapOf("matrix" to matrixList))
        //logger.info("inputJson: $inputJson")


        val processBuilder = ProcessBuilder("python", pythonScriptPath)
            .redirectErrorStream(true) // merge stdout and stderr

        val process = processBuilder.start()

        // Write input JSON to the python script's stdin
        process.outputStream.use { outputStream ->
            outputStream.write(inputJson.toByteArray())
            outputStream.flush()
        }

        // Read the output JSON from python script's stdout
        val output = process.inputStream.bufferedReader().use { it.readText() }

        // Wait for the process to finish (optional timeout 10 sec)
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroy()
            throw RuntimeException("Python process timed out")
        }

        if (process.exitValue() != 0) {
            throw RuntimeException("Python process exited with code ${process.exitValue()}: $output")
        }

        if (!output.trim().startsWith("{")) {
            throw IllegalArgumentException("Expected JSON output but got: $output")
        }

        // Deserialize output JSON into AsapResponse
        val result: AsapResponse = objectMapper.readValue(output)

        // Map nested list to list of Kotlin Pairs
        val pairs = result.pairs.map { Pair(it[0], it[1]) }

        return Pair(pairs, result.mean_eig)
    }
}
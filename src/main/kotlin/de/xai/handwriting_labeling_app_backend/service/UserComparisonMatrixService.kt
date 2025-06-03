package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import com.fasterxml.jackson.core.type.TypeReference

import java.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream
import java.util.zip.GZIPInputStream
import java.io.ByteArrayInputStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory



@Service
class UserComparisonMatrixService(
    private val matrixRepo: UserComparisonMatrixRepository,
    private val sampleRepository: SampleRepository,
    private val userRepository: UserRepository

) {
    private val logger = LoggerFactory.getLogger(javaClass)


    // GZIP Compression function
    fun compress(data: String): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { it.write(data.toByteArray()) }
        return byteArrayOutputStream.toByteArray()
    }

    // GZIP Decompression function
    fun decompress(compressedData: ByteArray): String {
        val byteArrayInputStream = ByteArrayInputStream(compressedData)
        val gzipInputStream = GZIPInputStream(byteArrayInputStream)
        val decompressedData = gzipInputStream.readBytes()
        return String(decompressedData)
    }

    // Base64 encoding (to convert ByteArray to String)
    fun encodeToBase64(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }

    // Base64 decoding (to convert String back to ByteArray)
    fun decodeFromBase64(encodedData: String): ByteArray {
        return Base64.getDecoder().decode(encodedData)
    }

    fun getMatrixForUser(username: String): Pair<Array<IntArray>, List<Long>> {
        val objectMapper = jacksonObjectMapper()

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")

        val entity = matrixRepo.findByUserId(user.id!!)

        return if (entity != null) {
            // Decompress and decode the matrix JSON
            val decompressedMatrixJson = decompress(decodeFromBase64(entity.matrixJson))
            logger.info("decompressed matrix: $decompressedMatrixJson")
            val matrix: Array<IntArray> = objectMapper.readValue(decompressedMatrixJson)
            logger.info("matrix: $matrix")

            // Decompress and decode the sample IDs JSON (no need for null checks)
            val decompressedSampleIdsJson = decompress(decodeFromBase64(entity.sampleIdsJson))
            val sampleIds: List<Long> = objectMapper.readValue(decompressedSampleIdsJson)

            Pair(matrix, sampleIds)
        } else {
            // No matrix exists, create a new one
            val samples = sampleRepository.findAll()
                .sortedBy { it.id }

            val sampleIds = samples.map { it.id }

            val newSize = samples.size
            val newMatrix = Array(newSize) { IntArray(newSize) }

            saveMatrixForUser(username, newMatrix, sampleIds)

            Pair(newMatrix, sampleIds)
        }
    }

    fun saveMatrixForUser(username: String, matrix: Array<IntArray>, samples: List<Long>) {
        val objectMapper = jacksonObjectMapper()
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")

        // Convert matrix and sample IDs to JSON strings
        val matrixJson = objectMapper.writeValueAsString(matrix)
        val sampleIdsJson = objectMapper.writeValueAsString(samples)

        // Compress both matrix and sample IDs JSON
        val compressedMatrixJson = compress(matrixJson)
        val compressedSampleIdsJson = compress(sampleIdsJson)

        // Encode the compressed byte arrays to Base64 strings
        val encodedMatrixJson = encodeToBase64(compressedMatrixJson)
        val encodedSampleIdsJson = encodeToBase64(compressedSampleIdsJson)

        val existing = matrixRepo.findByUserId(user.id!!)
        if (existing != null) {
            existing.matrixJson = encodedMatrixJson
            existing.sampleIdsJson = encodedSampleIdsJson
            matrixRepo.save(existing)
        } else {
            matrixRepo.save(UserComparisonMatrix(user = user, matrixJson = encodedMatrixJson, sampleIdsJson = encodedSampleIdsJson))
        }
    }

    fun recordComparison(username: String, winnerId: Long, loserId: Long) {
        val (matrix, sampleIds) = getMatrixForUser(username)

        // Find the indices for winner and loser in the sampleIds list
        val winnerIndex = sampleIds.indexOf(winnerId)
        val loserIndex = sampleIds.indexOf(loserId)

        // Ensure that the indices are within the bounds of sampleIds
        if (winnerIndex < 0 || winnerIndex >= sampleIds.size || loserIndex < 0 || loserIndex >= sampleIds.size) {
            throw IndexOutOfBoundsException("Invalid index. Ensure the indices are within the bounds of the sample list.")
        }

        matrix[winnerIndex][loserIndex] += 1

        saveMatrixForUser(username, matrix, sampleIds)
    }

    fun getAllMatrices(): List<UserComparisonMatrix> {
        return matrixRepo.findAll()
    }

}

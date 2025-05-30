package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.model.*
import de.xai.handwriting_labeling_app_backend.repository.*
import com.fasterxml.jackson.core.type.TypeReference


@Service
class UserComparisonMatrixService(
    private val matrixRepo: UserComparisonMatrixRepository,
    private val sampleRepository: SampleRepository,
    private val userRepository: UserRepository

) {
    fun getMatrixForUser(
        username: String
    ): Pair<Array<IntArray>, List<Long>> {

        val objectMapper = ObjectMapper()

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("No user found with username: $username")

        val entity = matrixRepo.findByUserId(user.id!!)

        return if (entity != null) {
            // Deserialize the matrix JSON into Array<IntArray>
            val matrix: Array<IntArray> = objectMapper.readValue(
                entity.matrixJson,
                object : TypeReference<Array<IntArray>>() {}
            )

            // Deserialize the sample IDs JSON into List<Long>
            val sampleIds: List<Long> = objectMapper.readValue(
                entity.sampleIdsJson,
                object : TypeReference<List<Long>>() {}
            )

            Pair(matrix, sampleIds)
        } else {
            // No matrix exists, create a new one
            // Fetch and sort all samples via sampleRepository
            val samples = sampleRepository.findAll()
                .sortedBy { it.id } // Ensure samples are sorted by id in ascending order


            // Populate the sampleIds list based on the sorted samples
            val sampleIds = samples.map { it.id } // Get IDs in the sorted order

            // Create a matrix with the size matching the number of samples
            val newSize = samples.size
            val newMatrix = Array(newSize) { IntArray(newSize) }

            // Save the new matrix and sample IDs
            saveMatrixForUser(username, newMatrix, sampleIds)

            // Return the new empty matrix and sample IDs
            Pair(newMatrix, sampleIds)
        }
    }



    fun saveMatrixForUser(username: String, matrix: Array<IntArray>, samples: List<Long>) {
        val objectMapper = ObjectMapper()
        val user = userRepository.findByUsername(username)
                ?: throw IllegalArgumentException("No user found with username: $username")

        val json = objectMapper.writeValueAsString(matrix)

        val sampleIdsJson = objectMapper.writeValueAsString(samples)
        val existing = matrixRepo.findByUserId(user.id!!)
        if (existing != null) {
            existing.matrixJson = json
            existing.sampleIdsJson = sampleIdsJson
            matrixRepo.save(existing)
        } else {
            matrixRepo.save(UserComparisonMatrix(user = user, matrixJson = json, sampleIdsJson = sampleIdsJson))
        }
    }


    fun recordComparison(username: String, winnerId: Long, loserId: Long) {
        // Fetch the matrix and sampleIds
        val (matrix, sampleIds) = getMatrixForUser(username)

        // Find the indices for winner and loser in the sampleIds list
        val winnerIndex = sampleIds.indexOf(winnerId)
        val loserIndex = sampleIds.indexOf(loserId)

        // Ensure that the indices are within the bounds of sampleIds
        if (winnerIndex < 0 || winnerIndex >= sampleIds.size || loserIndex < 0 || loserIndex >= sampleIds.size) {
            throw IndexOutOfBoundsException("Invalid index. Ensure the indices are within the bounds of the sample list.")
        }

        // Logic to update the matrix with winner and loser indices
        matrix[winnerIndex][loserIndex] += 1

        // Save the updated matrix and sample list
        saveMatrixForUser(username, matrix, sampleIds)
    }

    // This method will retrieve all UserComparisonMatrix records
    fun getAllMatrices(): List<UserComparisonMatrix> {
        return matrixRepo.findAll()
    }




}

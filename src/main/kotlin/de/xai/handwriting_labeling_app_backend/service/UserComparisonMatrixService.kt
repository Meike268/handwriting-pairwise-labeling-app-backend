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
        username: String,
        size: Int
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
            val emptyMatrix = Array(size) { IntArray(size) }
            saveMatrixForUser(username, emptyMatrix, listOf())
            Pair(emptyMatrix, listOf())
        }
        //return Pair(emptyArray(), emptyList<Long>())
    }

    fun saveMatrixForUser(username: String, matrix: Array<IntArray>, samples: List<Sample>) {
        val objectMapper = ObjectMapper()
        val user = userRepository.findByUsername(username)
                ?: throw IllegalArgumentException("No user found with username: $username")

        val json = objectMapper.writeValueAsString(matrix)

        // Get the sorted sample IDs, which correspond to matrix indices
        val sampleIds = samples.map { it.id }
        val sampleIdsJson = objectMapper.writeValueAsString(sampleIds)

        val existing = matrixRepo.findByUserId(user.id!!)
        if (existing != null) {
            existing.matrixJson = json
            existing.sampleIdsJson = sampleIdsJson
            matrixRepo.save(existing)
        } else {
            matrixRepo.save(UserComparisonMatrix(user = user, matrixJson = json, sampleIdsJson = sampleIdsJson))
        }
    }

    fun recordComparison(username: String, winnerId: Long, loserId: Long, size: Int) {
        val (matrix, sampleIds) = getMatrixForUser(username, size)

        val winnerIndex = sampleIds.indexOf(winnerId)
        val loserIndex = sampleIds.indexOf(loserId)

        // Logic to update the matrix with winner and loser indices
        matrix[winnerIndex][loserIndex] += 1
        saveMatrixForUser(username, matrix, fetchSamplesByIds(sampleIds)) // Fetch samples by IDs and save updated matrix
    }
    
    fun fetchSamplesByIds(sampleIds: List<Long>): List<Sample> {
        return sampleRepository.findAll()
    }
}

package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service


@Service
class UserComparisonMatrixService(
    private val matrixRepo: UserComparisonMatrixRepository
) {
    private val objectMapper = jacksonObjectMapper()

    fun getMatrixForUser(user: User, size: Int): Pair<Array<IntArray>, List<Long>> {
        val entity = matrixRepo.findByUser(user)
        return if (entity != null) {
            val matrix = objectMapper.readValue(entity.matrixJson)
            val sampleIds = objectMapper.readValue<List<Long>>(entity.sampleIdsJson)
            Pair(matrix, sampleIds)
        } else {
            val emptyMatrix = Array(size) { IntArray(size) }
            saveMatrixForUser(user, emptyMatrix, listOf()) // Empty sample list for now
            Pair(emptyMatrix, listOf())
        }
    }


    fun saveMatrixForUser(user: User, matrix: Array<IntArray>, samples: List<Sample>) {
        val json = objectMapper.writeValueAsString(matrix)

        // Get the sorted sample IDs, which correspond to matrix indices
        val sampleIds = samples.map { it.id }
        val sampleIdsJson = objectMapper.writeValueAsString(sampleIds)

        val existing = matrixRepo.findByUser(user)
        if (existing != null) {
            existing.matrixJson = json
            existing.sampleIdsJson = sampleIdsJson
            matrixRepo.save(existing)
        } else {
            matrixRepo.save(UserComparisonMatrix(user = user, matrixJson = json, sampleIdsJson = sampleIdsJson))
        }
    }


    fun recordComparison(user: User, winnerId: Long, loserId: Long, size: Int) {
        val (matrix, sampleIds) = getMatrixForUser(user, size)

        winnerIndex = sampleIds.indexOf(winnerId)
        loserIndex = sampleIds.indexOf(loserId)

        // Logic to update the matrix with winner and loser indices
        matrix[winnerIdx][loserIdx] += 1
        saveMatrixForUser(user, matrix, fetchSamplesByIds(sampleIds)) // Fetch samples by IDs and save updated matrix
    }
    
    fun fetchSamplesByIds(sampleIds: List<Long>): List<Sample> {
        return sampleRepository.findAllById(sampleIds)
    }
}

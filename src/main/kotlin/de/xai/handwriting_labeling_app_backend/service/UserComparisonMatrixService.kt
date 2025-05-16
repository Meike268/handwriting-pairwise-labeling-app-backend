package de.xai.handwriting_labeling_app_backend.service

import de.xai.handwriting_labeling_app_backend.model.UserComparisonMatrix
import de.xai.handwriting_labeling_app_backend.repository.UserComparisonMatrixRepository
import org.springframework.stereotype.Service


@Service
class UserComparisonMatrixService(
    private val matrixRepo: UserComparisonMatrixRepository
) {
    private val objectMapper = jacksonObjectMapper()

    fun getMatrixForUser(user: User, size: Int): Array<IntArray> {
        val entity = matrixRepo.findByUser(user)
        return if (entity != null) {
            objectMapper.readValue(entity.matrixJson)
        } else {
            val emptyMatrix = Array(size) { IntArray(size) }
            saveMatrixForUser(user, emptyMatrix)
            emptyMatrix
        }
    }

    fun saveMatrixForUser(user: User, matrix: Array<IntArray>) {
        val json = objectMapper.writeValueAsString(matrix)
        val existing = matrixRepo.findByUser(user)
        if (existing != null) {
            existing.matrixJson = json
            matrixRepo.save(existing)
        } else {
            matrixRepo.save(UserComparisonMatrix(user = user, matrixJson = json))
        }
    }

    fun recordComparison(user: User, winnerIdx: Int, loserIdx: Int, size: Int) {
        val matrix = getMatrixForUser(user, size)
        matrix[winnerIdx][loserIdx] += 1
        saveMatrixForUser(user, matrix)
    }
}

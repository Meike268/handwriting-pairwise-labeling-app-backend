import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import de.xai.handwriting_labeling_app_backend.service.UserComparisonMatrixService
import de.xai.handwriting_labeling_app_backend.model.Sample


@RestController
@RequestMapping("/matrices")
class UserComparisonMatrixController(private val matrixService: UserComparisonMatrixService) {

    // Endpoint to fetch matrix for a specific user
    @GetMapping("/{username}")
    fun getMatrix(@PathVariable username: String): ResponseEntity<Pair<Array<IntArray>, List<Long>>> {
        println("Received request for user: $username")
        try {
            val (matrix, sampleIds) = matrixService.getMatrixForUser(username, 10)  // assuming a default size of 10
            return ResponseEntity.ok(Pair(matrix, sampleIds))
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.notFound().build()
        }
    }

    // Endpoint to record a comparison between winner and loser
    @PostMapping("/{username}/comparison")
    fun recordComparison(
        @PathVariable username: String,
        @RequestParam winnerId: Long,
        @RequestParam loserId: Long,
        @RequestParam size: Int
    ): ResponseEntity<Void> {
        try {
            matrixService.recordComparison(username, winnerId, loserId, size)
            return ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }

    // Endpoint to save a matrix for a user
    @PostMapping("/{username}/save")
    fun saveMatrix(
        @PathVariable username: String,
        @RequestBody matrix: Array<IntArray>,
        @RequestBody samples: List<Sample>
    ): ResponseEntity<Void> {
        try {
            matrixService.saveMatrixForUser(username, matrix, samples)
            return ResponseEntity.ok().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }
}

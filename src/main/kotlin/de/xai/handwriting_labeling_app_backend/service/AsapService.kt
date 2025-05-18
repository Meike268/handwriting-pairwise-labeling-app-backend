data class AsapResponse(
    val pairs: List<List<Int>>,
    val max_eig: Double
)

@Service
class AsapClient {
    private val objectMapper = jacksonObjectMapper()

    fun getPairsToCompare(matrix: Array<IntArray>): Pair<List<Pair<Int, Int>>, Double> {
        val pythonScriptPath = "src/main/kotlin/de/xai/handwriting_labeling_app_backend/utils/asap_runner.py"

        val inputJson = objectMapper.writeValueAsString(mapOf("matrix" to matrix))
        val process = ProcessBuilder("python", pythonScriptPath)
            .redirectErrorStream(true)
            .start()

        process.outputStream.use {
            it.write(inputJson.toByteArray())
            it.flush()
        }

        val output = process.inputStream.bufferedReader().readText()
        val result = objectMapper.readValue(output, AsapResponse::class.java)

        val pairs = result.pairs.map { Pair(it[0], it[1]) }
        return Pair(pairs, result.max_eig)
    }
}
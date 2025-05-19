data class AsapResponse(
    val pairs: List<List<Int>>,
    val max_eig: Double
)

@Service
class AsapService {
    private val objectMapper = jacksonObjectMapper()

    fun getPairsToCompare(matrix: Array<IntArray>): Pair<List<Pair<Int, Int>>, Double> {
        val pythonScriptPath = "src/main/kotlin/de/xai/handwriting_labeling_app_backend/utils/asap_runner.py"

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
        val pairs = result.pairs.map { Pair(it[0], it[1]) }


        return Pair(pairs, result.max_eig)
    }
}
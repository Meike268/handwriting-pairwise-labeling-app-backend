package de.xai.handwriting_labeling_app_backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.apimodel.GetBatchResponseBody
import de.xai.handwriting_labeling_app_backend.service.BatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping("/batch")
class BatchController(
    private val batchService: BatchService
) {
    @GetMapping
    fun getRandomBatch(principal: Principal, @RequestParam("excludedTasks") excludedTasks: String): GetBatchResponseBody {
        val excludedTasksMap: Map<Long, List<Long>> = ObjectMapper().readTree(excludedTasks)
            .fields().asSequence().asIterable()
            .associate {
                it.key.toLong() to it.value.toList().map { it.asLong() }
            }

        return batchService.generateBatch(principal.name, excludedTasksMap)
    }
}
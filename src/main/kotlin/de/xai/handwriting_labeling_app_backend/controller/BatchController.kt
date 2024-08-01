package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.GetBatchResponseBody
import de.xai.handwriting_labeling_app_backend.service.BatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/batch")
class BatchController(
    private val batchService: BatchService
) {
    @GetMapping
    fun getRandomBatch(principal: Principal): GetBatchResponseBody {
        return batchService.generateBatch(principal.name)
    }
}
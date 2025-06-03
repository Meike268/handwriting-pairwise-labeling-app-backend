package de.xai.handwriting_labeling_app_backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.xai.handwriting_labeling_app_backend.apimodel.GetBatchResponseBody
import de.xai.handwriting_labeling_app_backend.service.BatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

import de.xai.handwriting_labeling_app_backend.utils.Constants.Companion.GET_BATCH_RESPONSE_STATE_SUCCESS
import org.slf4j.LoggerFactory



@RestController
@RequestMapping("/batch")
class BatchController(
    private val batchService: BatchService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getRandomBatch(principal: Principal): GetBatchResponseBody {
        return batchService.generateBatch(principal.name)
    }
}


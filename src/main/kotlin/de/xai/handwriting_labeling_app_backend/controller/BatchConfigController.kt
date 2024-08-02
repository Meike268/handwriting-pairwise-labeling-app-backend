package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.component.BatchConfigHandler
import de.xai.handwriting_labeling_app_backend.model.BatchServiceConfig
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/config")
class BatchConfigController(
    private val batchConfigHandler: BatchConfigHandler
) {

    @GetMapping
    fun getConfig(): BatchServiceConfig {
        return batchConfigHandler.readBatchServiceConfig()
    }

    @PostMapping
    fun postConfig(@RequestBody batchConfig: BatchServiceConfig): BatchServiceConfig {
        return batchConfigHandler.writeBatchServiceConfig(batchConfig)
    }
}
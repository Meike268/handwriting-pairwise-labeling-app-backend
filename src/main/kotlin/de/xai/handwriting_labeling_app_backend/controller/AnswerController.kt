package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.AnswerCreateBody
import de.xai.handwriting_labeling_app_backend.apimodel.ExportAnswersBody
import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.service.AnswerService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/answers")
class AnswerController(
    private val answerService: AnswerService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun createAnswer(principal: Principal, @RequestBody answer: AnswerCreateBody): ResponseEntity<Answer> {
        logger.info("Received new answer: $answer from user ${principal.name}")

        return ResponseEntity.ok(
            answerService.createOrUpdate(
                principal.name,
                answer.sampleId,
                answer.questionId,
                answer.score,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(answer.submissionTimestamp), TimeZone.getDefault().toZoneId())
            )
        )
    }

    @PutMapping
    fun updateAnswer(principal: Principal, @RequestBody answer: AnswerCreateBody): ResponseEntity<Answer> {
        logger.info("Updating answer: $answer from user ${principal.name}\"")

        return ResponseEntity.ok(answerService.createOrUpdate(
            principal.name,
            answer.sampleId,
            answer.questionId,
            answer.score,
            LocalDateTime.ofInstant(Instant.ofEpochMilli(answer.submissionTimestamp), TimeZone.getDefault().toZoneId())
        ))
    }

    @GetMapping
    fun getAnswers(
        @RequestParam(name = "xai")
        xai: Boolean = false ,
        @RequestParam(name = "others")
        others: Boolean = false

    ): ResponseEntity<ExportAnswersBody> {
        logger.info("Exporting answers and metadata.")
        return ResponseEntity.ok(
            answerService.getAnswers(xai, others)
        )
    }

    @DeleteMapping("/ofsample/{id}")
    fun deleteAnswersOfSample(@PathVariable id: Long): ResponseEntity<String> {
        logger.info("Deleting answers to sample with: $id")

        val deletedAllSuccessfully = answerService.deleteAnswersOfSample(id)

        if (deletedAllSuccessfully) {
            logger.debug("Successfully deleted all answers")
            return ResponseEntity.ok("Successfully deleted all answers")
        } else {
            logger.debug("Could not delete all answers that were found for sample with id: $id")
            return ResponseEntity.ok("Did not delete all answers")
        }
    }
}
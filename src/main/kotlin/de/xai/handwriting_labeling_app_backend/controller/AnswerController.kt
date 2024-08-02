package de.xai.handwriting_labeling_app_backend.controller

import de.xai.handwriting_labeling_app_backend.apimodel.AnswerCreateBody
import de.xai.handwriting_labeling_app_backend.apimodel.ExportAnswersBody
import de.xai.handwriting_labeling_app_backend.model.Answer
import de.xai.handwriting_labeling_app_backend.service.AnswerService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/answers")
class AnswerController(
    private val answerService: AnswerService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    fun createAnswer(principal: Principal, @RequestBody answer: AnswerCreateBody): ResponseEntity<Answer> {
        logger.info("Received new answer: $answer")

        return ResponseEntity.ok(
            answerService.createOrUpdate(
                principal.name,
                answer.sampleId,
                answer.questionId,
                answer.score
            )
        )
    }

    @PutMapping
    fun updateAnswer(principal: Principal, @RequestBody answer: AnswerCreateBody): ResponseEntity<Answer> {
        logger.info("Updating answer: $answer")

        return ResponseEntity.ok(answerService.createOrUpdate(
            principal.name,
            answer.sampleId,
            answer.questionId,
            answer.score
        ))
    }

    @GetMapping("/xai_sentences")
    fun getAnswers(
        @RequestParam("xai") includeXAi: Boolean,
        @RequestParam("others") includeOthers: Boolean
    ): ResponseEntity<ExportAnswersBody> {
        return ResponseEntity.ok(
            answerService.getAnswers(includeXAi, includeOthers)
        )
    }
}
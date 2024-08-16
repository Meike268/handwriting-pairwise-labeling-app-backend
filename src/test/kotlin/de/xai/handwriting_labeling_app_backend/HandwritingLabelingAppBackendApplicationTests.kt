package de.xai.handwriting_labeling_app_backend

import de.xai.handwriting_labeling_app_backend.repository.AnswerRepository
import de.xai.handwriting_labeling_app_backend.repository.SampleRepository
import de.xai.handwriting_labeling_app_backend.repository.UserRepository
import de.xai.handwriting_labeling_app_backend.service.AnswerService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class HandwritingLabelingAppBackendApplicationTests(
	val answerService: AnswerService,
	val sampleRepository: SampleRepository
) {

	@Test
	fun contextLoads() {
	}

	@Test
	fun fillAnswersTable() {

		val allSamples = sampleRepository.findAll()
		for (sample in allSamples) {

			answerService.createOrUpdate(
				"user",
				sampleId = sample.id,
				questionId = 1,
				score = 3,
				submissionTimestamp = LocalDateTime.now()
			)
		}
	}

}

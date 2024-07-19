package de.xai.handwriting_labeling_app_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class HandwritingLabelingAppBackendApplication

fun main(args: Array<String>) {
	runApplication<HandwritingLabelingAppBackendApplication>(*args)
}
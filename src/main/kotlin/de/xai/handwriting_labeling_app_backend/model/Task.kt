package de.xai.handwriting_labeling_app_backend.model

data class Task (val sample: Sample, val question: Question) {
    override fun toString(): String {
        return "Task(sample=${sample.id}, question=${question.id})"
    }
}
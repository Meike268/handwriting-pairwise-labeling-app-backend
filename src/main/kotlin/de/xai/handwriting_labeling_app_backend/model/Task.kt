package de.xai.handwriting_labeling_app_backend.model

/**
* adjust such that each task holds two samples
**/
data class Task (val sample1: Sample, val sample2: Sample, val question: Question) {
    override fun toString(): String {
        return "Task(sample1=${sample1.id}, sample2=${sample2.id}, question=${question.id})"
    }
}
package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String,
    val title: String = "",
    val moduleId: String = "",
    val lessonId: String = "",
    val lessonRefPath: String = "",
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val type: String = "",
    val difficulty: String = "",
    val difficultyWeight: Double = 0.0,
    val tags: List<String> = emptyList(),
    val format: String = "single_choice",
    val quizEnabled: Boolean = true,
    val randomKey: Long = 0L,
    val orderIndex: Int = 0,
    val seedVersion: String = "",
    val codeSnippet: String? = null
) {
    val questionId: String get() = id
}

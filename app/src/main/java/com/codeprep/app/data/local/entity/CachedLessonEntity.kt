package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codeprep.app.data.local.CodeSnippet

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val title: String,
    val content: String,
    val analogy: String?,
    val keyPoints: List<String>, // Koristi TypeConverter
    val commonMistakes: List<String>, // Koristi TypeConverter
    val codeSnippets: List<CodeSnippet>, // Koristi TypeConverter
    val orderIndex: Int,
    val xpReward: Int,
    val questionCount: Int
)
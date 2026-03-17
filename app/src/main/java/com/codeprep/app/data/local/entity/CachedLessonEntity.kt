package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codeprep.app.data.local.CodeSnippet
import com.codeprep.app.data.model.LessonContentBlock
import com.codeprep.app.data.model.LocalizedText

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val title: LocalizedText,
    val orderIndex: Int,
    val xpReward: Int,
    val questionCount: Int,
    val content: LessonContentBlock,
    val analogy: LocalizedText?,
    val keyPoints: List<LocalizedText>,
    val commonMistakes: List<LocalizedText>,
    val codeSnippets: List<CodeSnippet>
)

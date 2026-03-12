package com.codeprep.app.data.local.entity

import androidx.room.Entity
import java.time.Instant

@Entity(
    tableName = "lesson_progress",
    primaryKeys = ["userId", "lessonId"]
)
data class LessonProgressEntity(
    val userId: String,
    val lessonId: String,
    val completed: Boolean,
    val perfectRun: Boolean,
    val score: Int,
    val mistakeCount: Int,
    val lastAttemptAt: Instant?
)
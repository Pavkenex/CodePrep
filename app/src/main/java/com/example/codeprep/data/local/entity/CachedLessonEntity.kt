package com.example.codeprep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_lessons")
data class CachedLessonEntity(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val title: String,
    val summary: String
)
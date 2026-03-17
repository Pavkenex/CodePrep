package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.codeprep.app.data.model.LocalizedText

@Entity(tableName = "cached_courses")
data class CachedCourseEntity(
    @PrimaryKey val courseId: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val orderIndex: Int,
    val isLocked: Boolean,
    val lessonCount: Int,
    val icon: String
)

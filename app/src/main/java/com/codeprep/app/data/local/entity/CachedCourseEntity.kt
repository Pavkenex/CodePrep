package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_courses")
data class CachedCourseEntity(
    @PrimaryKey val courseId: String,
    val title: String,
    val description: String,
    val order: Int
)
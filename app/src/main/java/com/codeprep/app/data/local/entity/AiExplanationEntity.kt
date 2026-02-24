package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_explanations")
data class AiExplanationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val contextLessonId: String?,
    val answer: String,
    val cachedAt: Long
)
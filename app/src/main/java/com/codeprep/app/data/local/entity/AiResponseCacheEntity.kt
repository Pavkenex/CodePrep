package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_response_cache",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "question", "contextLessonId"]),
        Index(value = ["userId", "cachedAt"])
    ]
)
data class AiResponseCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val question: String,
    val contextLessonId: String?,
    val answer: String,
    val cachedAt: Long
)

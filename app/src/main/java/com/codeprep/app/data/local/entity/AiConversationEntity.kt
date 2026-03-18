package com.codeprep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_conversations",
    indices = [
        Index(value = ["userId", "lessonId"], unique = true),
        Index(value = ["userId", "updatedAt"])
    ]
)
data class AiConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val lessonId: String,
    val courseTitle: String,
    val lessonTitle: String,
    val createdAt: Long,
    val updatedAt: Long
)

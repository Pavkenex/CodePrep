package com.codeprep.app.domain.model

enum class AiConversationRole {
    User,
    Assistant,
    System
}

data class AiConversationMessage(
    val id: String,
    val role: AiConversationRole,
    val content: String,
    val createdAt: Long
)

data class SavedAiConversation(
    val id: String,
    val lessonId: String,
    val courseTitle: String,
    val lessonTitle: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<AiConversationMessage>
)

data class SavedAiConversationSummary(
    val id: String,
    val lessonId: String,
    val courseTitle: String,
    val lessonTitle: String,
    val preview: String,
    val messageCount: Int,
    val updatedAt: Long
)

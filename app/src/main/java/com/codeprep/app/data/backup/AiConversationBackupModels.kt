package com.codeprep.app.data.backup

import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.domain.model.SavedAiConversation
import com.google.gson.Gson

private const val CURRENT_BACKUP_VERSION = 1

data class ImportedAiConversationBackup(
    val lessonId: String,
    val courseTitle: String,
    val lessonTitle: String,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<AiConversationMessage>
)

fun serializeAiConversationBackup(conversations: List<SavedAiConversation>): String {
    val payload = AiConversationBackupPayload(
        version = CURRENT_BACKUP_VERSION,
        conversations = conversations.map { conversation ->
            AiConversationBackupConversation(
                lessonId = conversation.lessonId,
                courseTitle = conversation.courseTitle,
                lessonTitle = conversation.lessonTitle,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt,
                messages = conversation.messages.map { message ->
                    AiConversationBackupMessage(
                        role = message.role.name,
                        content = message.content,
                        createdAt = message.createdAt
                    )
                }
            )
        }
    )
    return Gson().toJson(payload)
}

fun deserializeAiConversationBackup(json: String): List<ImportedAiConversationBackup> {
    val payload = Gson().fromJson(json, AiConversationBackupPayload::class.java)
        ?: throw IllegalArgumentException("Conversation backup is empty")

    require(payload.version == CURRENT_BACKUP_VERSION) {
        "Unsupported conversation backup version: ${payload.version}"
    }

    return payload.conversations.orEmpty().map { conversation ->
        ImportedAiConversationBackup(
            lessonId = conversation.lessonId.orEmpty(),
            courseTitle = conversation.courseTitle.orEmpty(),
            lessonTitle = conversation.lessonTitle.orEmpty(),
            createdAt = conversation.createdAt ?: 0L,
            updatedAt = conversation.updatedAt ?: 0L,
            messages = conversation.messages.orEmpty().sortedBy { it.createdAt ?: 0L }.map { message ->
                AiConversationMessage(
                    id = "imported-${conversation.lessonId.orEmpty()}-${message.createdAt ?: 0L}-${message.role.orEmpty()}",
                    role = message.role.toConversationRole(),
                    content = message.content.orEmpty(),
                    createdAt = message.createdAt ?: 0L
                )
            }
        )
    }
}

private fun String?.toConversationRole(): AiConversationRole {
    return when (this) {
        AiConversationRole.Assistant.name -> AiConversationRole.Assistant
        AiConversationRole.System.name -> AiConversationRole.System
        else -> AiConversationRole.User
    }
}

private data class AiConversationBackupPayload(
    val version: Int = CURRENT_BACKUP_VERSION,
    val conversations: List<AiConversationBackupConversation>? = emptyList()
)

private data class AiConversationBackupConversation(
    val lessonId: String? = null,
    val courseTitle: String? = null,
    val lessonTitle: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val messages: List<AiConversationBackupMessage>? = emptyList()
)

private data class AiConversationBackupMessage(
    val role: String? = null,
    val content: String? = null,
    val createdAt: Long? = null
)

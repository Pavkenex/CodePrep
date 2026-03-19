package com.codeprep.app.data.backup

import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.domain.model.SavedAiConversation
import org.junit.Assert.assertEquals
import org.junit.Test

class AiConversationBackupModelsTest {

    @Test
    fun serializeAndDeserializeAiConversationBackup_roundTripsSavedConversations_inMessageOrder() {
        val payload = serializeAiConversationBackup(
            listOf(
                savedConversation(
                    id = "conversation-1",
                    lessonId = "two-sum",
                    createdAt = 100L,
                    updatedAt = 200L,
                    messages = listOf(
                        message("m1", AiConversationRole.User, "What is the idea?", 101L),
                        message("m2", AiConversationRole.Assistant, "Use complements.", 102L)
                    )
                ),
                savedConversation(
                    id = "conversation-2",
                    lessonId = "depth",
                    createdAt = 300L,
                    updatedAt = 400L,
                    messages = listOf(
                        message("m3", AiConversationRole.User, "How deep?", 301L),
                        message("m4", AiConversationRole.Assistant, "Use DFS.", 302L)
                    )
                )
            )
        )

        val restored = deserializeAiConversationBackup(payload)

        assertEquals(listOf("two-sum", "depth"), restored.map { it.lessonId })
        assertEquals(
            listOf(AiConversationRole.User, AiConversationRole.Assistant),
            restored.first().messages.map { it.role }
        )
        assertEquals(
            listOf("What is the idea?", "Use complements."),
            restored.first().messages.map { it.content }
        )
        assertEquals(300L, restored.last().createdAt)
        assertEquals(400L, restored.last().updatedAt)
    }

    @Test
    fun deserializeAiConversationBackup_rejectsUnsupportedVersion() {
        val payload = """
            {
              "version": 99,
              "conversations": []
            }
        """.trimIndent()

        val error = runCatching { deserializeAiConversationBackup(payload) }.exceptionOrNull()

        requireNotNull(error)
        assertEquals("Unsupported conversation backup version: 99", error.message)
    }

    private fun savedConversation(
        id: String,
        lessonId: String,
        createdAt: Long,
        updatedAt: Long,
        messages: List<AiConversationMessage>
    ): SavedAiConversation {
        return SavedAiConversation(
            id = id,
            lessonId = lessonId,
            courseTitle = "Module for $lessonId",
            lessonTitle = "Lesson $lessonId",
            createdAt = createdAt,
            updatedAt = updatedAt,
            messages = messages
        )
    }

    private fun message(
        id: String,
        role: AiConversationRole,
        content: String,
        createdAt: Long
    ): AiConversationMessage {
        return AiConversationMessage(
            id = id,
            role = role,
            content = content,
            createdAt = createdAt
        )
    }
}

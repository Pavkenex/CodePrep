package com.codeprep.app.ui.ai

import androidx.compose.runtime.Immutable
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole

@Immutable
data class AskAiUiState(
    val courseTitle: String = "",
    val lessonTitle: String = "",
    val messages: List<AiConversationMessage> = emptyList(),
    val isLoading: Boolean = false,
    val hasLoadedLesson: Boolean = false,
    val isSaved: Boolean = false,
    val hasDraft: Boolean = false,
    val canSave: Boolean = false
)

@Immutable
data class ExplanationsUiState(
    val modules: List<ExplanationsModuleUi> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

internal fun conversationMessage(
    role: AiConversationRole,
    content: String,
    createdAt: Long = System.currentTimeMillis()
): AiConversationMessage {
    return AiConversationMessage(
        id = "${role.name.lowercase()}-$createdAt-${content.hashCode()}",
        role = role,
        content = content,
        createdAt = createdAt
    )
}

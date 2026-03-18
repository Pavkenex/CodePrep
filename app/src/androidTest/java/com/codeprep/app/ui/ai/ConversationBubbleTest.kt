package com.codeprep.app.ui.ai

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.ui.theme.CodePrepTheme
import org.junit.Rule
import org.junit.Test

class ConversationBubbleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assistantBubble_rendersMarkdownInsteadOfRawSyntax() {
        val message = AiConversationMessage(
            id = "assistant-1",
            role = AiConversationRole.Assistant,
            content = "Hello **world**",
            createdAt = 0L
        )

        composeTestRule.setContent {
            CodePrepTheme {
                ConversationBubble(message = message)
            }
        }

        composeTestRule.onNodeWithText("Hello **world**").assertDoesNotExist()
        composeTestRule.onNodeWithText("Hello world").assertExists()
    }

    @Test
    fun assistantBubble_rendersFencedCodeWithHeaderAndCopyAction() {
        val message = AiConversationMessage(
            id = "assistant-code",
            role = AiConversationRole.Assistant,
            content = """
                ```python
                class Animal:
                    def speak(self):
                        print("Some sound")
                ```
            """.trimIndent(),
            createdAt = 0L
        )

        composeTestRule.setContent {
            CodePrepTheme {
                ConversationBubble(message = message)
            }
        }

        composeTestRule.onNodeWithText("```python").assertDoesNotExist()
        composeTestRule.onNodeWithText("Python").assertExists()
        composeTestRule.onNodeWithText("Copy").assertExists()
        composeTestRule.onNodeWithText("KOTLIN").assertDoesNotExist()
    }

    @Test
    fun userBubble_keepsRawMarkdownSyntaxAsPlainText() {
        val message = AiConversationMessage(
            id = "user-1",
            role = AiConversationRole.User,
            content = "Hello **world**",
            createdAt = 0L
        )

        composeTestRule.setContent {
            CodePrepTheme {
                ConversationBubble(message = message)
            }
        }

        composeTestRule.onNodeWithText("Hello **world**").assertExists()
    }
}

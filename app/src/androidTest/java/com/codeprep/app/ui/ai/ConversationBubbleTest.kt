package com.codeprep.app.ui.ai

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.ui.theme.CodePrepTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

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
    fun assistantBubble_usesWarmGrayBodyTextColor() {
        val message = AiConversationMessage(
            id = "assistant-prose",
            role = AiConversationRole.Assistant,
            content = "Warm gray body text should be easy on the eyes.",
            createdAt = 0L
        )

        composeTestRule.setContent {
            CodePrepTheme {
                ConversationBubble(message = message)
            }
        }

        composeTestRule.waitForIdle()

        val image = composeTestRule.onNodeWithText(
            "Warm gray body text should be easy on the eyes."
        ).captureToImage()
        val pixelMap = image.toPixelMap()

        assertTrue(
            "Expected assistant prose to render with the warm gray body text color.",
            pixelMap.containsColorNear(Color(0xFFD9DEE3), tolerance = 0.025f)
        )
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

    private fun androidx.compose.ui.graphics.PixelMap.containsColorNear(
        expected: Color,
        tolerance: Float
    ): Boolean {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = this[x, y]
                if (
                    pixel.alpha > 0.9f &&
                    abs(pixel.red - expected.red) <= tolerance &&
                    abs(pixel.green - expected.green) <= tolerance &&
                    abs(pixel.blue - expected.blue) <= tolerance
                ) {
                    return true
                }
            }
        }
        return false
    }
}

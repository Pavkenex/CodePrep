package com.codeprep.app.ui.lesson

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.codeprep.app.data.local.CodeSnippet
import com.codeprep.app.data.model.LocalizedText
import com.codeprep.app.ui.theme.CodePrepTheme
import com.codeprep.app.ui.theme.ElectricCyan
import org.junit.Rule
import org.junit.Test

class LessonSnippetBlockTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lessonSnippetBlock_rendersHeaderAndCopyActionForPseudocode() {
        val snippet = CodeSnippet(
            language = "pseudocode",
            description = LocalizedText(en = "Walk through the algorithm."),
            code = """
                FOR each item IN items
                    PRINT item
                END FOR
            """.trimIndent(),
            isAntiPattern = false
        )

        composeTestRule.setContent {
            CodePrepTheme {
                LessonSnippetBlock(
                    label = "Pseudocode",
                    accent = ElectricCyan,
                    snippet = snippet,
                    language = "en"
                )
            }
        }

        composeTestRule.onNodeWithText("Copy").assertExists()
        composeTestRule.onAllNodesWithText("Pseudocode").assertCountEquals(2)
        composeTestRule.onNodeWithText("Walk through the algorithm.").assertExists()
    }
}

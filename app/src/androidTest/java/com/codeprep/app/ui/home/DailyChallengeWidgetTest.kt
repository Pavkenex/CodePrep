package com.codeprep.app.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.ui.theme.CodePrepTheme
import org.junit.Rule
import org.junit.Test

class DailyChallengeWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun expandedDailyChallenge_usesSharedCodeBlockForSnippet() {
        val question = Question(
            id = "daily-1",
            text = "What does this code print?",
            options = listOf("0", "1", "2"),
            correctIndex = 1,
            explanation = "Because the value increments once.",
            codeSnippetLanguage = "kotlin",
            codeSnippet = """
                val count = 0
                println(count + 1)
            """.trimIndent()
        )

        composeTestRule.setContent {
            CodePrepTheme {
                DailyChallengeWidget(
                    dailyState = DailyChallengeUiState(
                        question = question,
                        isLoading = false,
                        isExpanded = true
                    ),
                    onExpand = {},
                    onAnswer = {},
                    onComplete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Kotlin").assertExists()
        composeTestRule.onNodeWithText("Copy").assertExists()
    }
}

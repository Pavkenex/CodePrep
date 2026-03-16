package com.codeprep.app.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class TopBarStatsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun topBarStats_hidesHeartTimerWhenNoTimerTextProvided() {
        composeTestRule.setContent {
            TopBarStats(
                hearts = 5,
                streak = 7,
                heartTimerText = null
            )
        }

        composeTestRule.onNodeWithText("5").assertExists()
        composeTestRule.onNodeWithText("7").assertExists()
        composeTestRule.onNodeWithText("Next in 12:43").assertDoesNotExist()
    }

    @Test
    fun topBarStats_showsHeartTimerWhenProvided() {
        composeTestRule.setContent {
            TopBarStats(
                hearts = 3,
                streak = 7,
                heartTimerText = "Next in 12:43"
            )
        }

        composeTestRule.onNodeWithText("3").assertExists()
        composeTestRule.onNodeWithText("Next in 12:43").assertExists()
    }
}

package com.codeprep.app.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.codeprep.app.feedback.AppFeedback
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GamifiedButtonFeedbackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gamifiedButton_emitsTapPrimaryFeedbackBeforeOnClick() {
        val recorder = ButtonFeedbackRecorder()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppFeedback provides recorder) {
                GamifiedButton(
                    text = "Start",
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Start").performClick()

        assertEquals(listOf(FeedbackEvent.TapPrimary), recorder.events)
    }
}

private class ButtonFeedbackRecorder : AppFeedback {
    val events = mutableListOf<FeedbackEvent>()

    override fun emit(event: FeedbackEvent) {
        events += event
    }
}

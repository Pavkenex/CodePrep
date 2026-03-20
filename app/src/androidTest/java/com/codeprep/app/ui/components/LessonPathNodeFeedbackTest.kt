package com.codeprep.app.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.codeprep.app.feedback.AppFeedback
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LessonPathNodeFeedbackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeLessonPathNode_emitsTapPrimaryFeedbackBeforeOnClick() {
        val recorder = LessonNodeFeedbackRecorder()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppFeedback provides recorder) {
                LessonPathNode(
                    state = NodeState.ACTIVE,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Active").performClick()

        assertEquals(listOf(FeedbackEvent.TapPrimary), recorder.events)
    }
}

private class LessonNodeFeedbackRecorder : AppFeedback {
    val events = mutableListOf<FeedbackEvent>()

    override fun emit(event: FeedbackEvent) {
        events += event
    }
}

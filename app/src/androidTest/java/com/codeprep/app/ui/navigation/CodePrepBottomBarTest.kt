package com.codeprep.app.ui.navigation

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

class CodePrepBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomBar_emitsTapFeedbackAndNavigatesToUnselectedRoute() {
        val recorder = RecordingAppFeedback()
        var navigatedRoute: String? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppFeedback provides recorder) {
                CodePrepBottomBar(
                    currentRoute = Screen.Home.route,
                    onNavigate = { navigatedRoute = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Modules").performClick()

        assertEquals(listOf(FeedbackEvent.TapPrimary), recorder.events)
        assertEquals(Screen.CourseList.route, navigatedRoute)
    }
}

private class RecordingAppFeedback : AppFeedback {
    val events = mutableListOf<FeedbackEvent>()

    override fun emit(event: FeedbackEvent) {
        events += event
    }
}

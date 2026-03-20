package com.codeprep.app.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class ProvideAppFeedbackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun provideAppFeedback_exposesInjectedFeedbackThroughCompositionLocal() {
        val expected = RecordingAppFeedback()
        var provided: AppFeedback? = null

        composeTestRule.setContent {
            ProvideAppFeedback(appFeedback = expected) {
                provided = currentAppFeedback()
            }
        }

        composeTestRule.runOnIdle {
            assertSame(expected, provided)
        }
    }
}

private class RecordingAppFeedback : AppFeedback {
    override fun emit(event: FeedbackEvent) = Unit
}

@Composable
private fun currentAppFeedback(): AppFeedback = LocalAppFeedback.current

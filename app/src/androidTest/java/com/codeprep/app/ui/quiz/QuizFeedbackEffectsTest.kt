package com.codeprep.app.ui.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.feedback.AppFeedback
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizFeedbackEffectsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quizFeedbackEffects_emitsSuccessWhenAnswerBecomesCorrectlyAnswered() {
        val recorder = QuizFeedbackRecorder()
        var state by mutableStateOf(quizState(isAnswered = false, selectedIndex = null))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppFeedback provides recorder) {
                QuizFeedbackEffects(state = state)
            }
        }

        composeTestRule.runOnIdle {
            state = quizState(isAnswered = true, selectedIndex = 1)
        }

        composeTestRule.runOnIdle {
            assertEquals(listOf(FeedbackEvent.Success), recorder.events)
        }
    }

    @Test
    fun quizFeedbackEffects_emitsErrorWhenAnswerBecomesIncorrectlyAnswered() {
        val recorder = QuizFeedbackRecorder()
        var state by mutableStateOf(quizState(isAnswered = false, selectedIndex = null))

        composeTestRule.setContent {
            CompositionLocalProvider(LocalAppFeedback provides recorder) {
                QuizFeedbackEffects(state = state)
            }
        }

        composeTestRule.runOnIdle {
            state = quizState(isAnswered = true, selectedIndex = 0)
        }

        composeTestRule.runOnIdle {
            assertEquals(listOf(FeedbackEvent.Error), recorder.events)
        }
    }
}

private class QuizFeedbackRecorder : AppFeedback {
    val events = mutableListOf<FeedbackEvent>()

    override fun emit(event: FeedbackEvent) {
        events += event
    }
}

private fun quizState(
    isAnswered: Boolean,
    selectedIndex: Int?
): QuizUiState = QuizUiState(
    questions = listOf(
        Question(
            id = "question-1",
            text = "What is 1 + 1?",
            options = listOf("1", "2"),
            correctIndex = 1,
            explanation = "2"
        )
    ),
    selectedIndex = selectedIndex,
    isAnswered = isAnswered
)

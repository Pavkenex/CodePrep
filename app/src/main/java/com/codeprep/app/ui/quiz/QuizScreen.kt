package com.codeprep.app.ui.quiz

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.R
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LeafGreen
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = hiltViewModel(),
    onQuizFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuizFeedbackEffects(state = state)

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = localizedStringResource(R.string.quiz_hearts, state.userHearts),
                    style = MaterialTheme.typography.titleLarge,
                    color = CardinalRed
                )
                Text(
                    text = localizedStringResource(
                        R.string.quiz_score,
                        state.score,
                        state.questions.size
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricCyan
                )
            }
        }
    ) { paddingValues ->
        if (state.finished) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Charcoal,
                titleContentColor = ElectricCyan,
                textContentColor = TextLight,
                title = {
                    Text(
                        text = when {
                            state.perfect -> localizedStringResource(R.string.quiz_result_perfect_title)
                            state.passed -> localizedStringResource(R.string.quiz_result_passed_title)
                            else -> localizedStringResource(R.string.quiz_result_finished_title)
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val totalQuestions = state.questions.size
                        val percentage = if (totalQuestions > 0) {
                            (state.score.toFloat() / totalQuestions * 100).toInt()
                        } else {
                            0
                        }

                        Text(
                            text = localizedStringResource(R.string.quiz_result_percentage, percentage),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ElectricCyan
                        )
                        Text(
                            text = localizedStringResource(
                                R.string.quiz_result_correct_answers,
                                state.score,
                                totalQuestions
                            ),
                            color = TextLight
                        )
                        Text(
                            text = when {
                                state.perfect -> localizedStringResource(R.string.quiz_result_star_earned)
                                state.passed -> localizedStringResource(R.string.quiz_result_next_lesson_unlocked)
                                else -> localizedStringResource(R.string.quiz_result_unlock_requirement)
                            },
                            color = if (state.passed) ElectricCyan else CardinalRed
                        )
                        if (state.awardedBaseXp) {
                            Text(
                                text = localizedStringResource(
                                    R.string.quiz_result_base_reward,
                                    state.lessonXpReward
                                ),
                                color = LeafGreen
                            )
                        }
                        if (state.awardedPerfectBonus) {
                            Text(
                                text = localizedStringResource(
                                    R.string.quiz_result_perfect_bonus,
                                    state.perfectBonusXp
                                ),
                                color = SunYellow
                            )
                        }
                        if (state.xpEarned == 0) {
                            Text(
                                text = when {
                                    state.perfect -> localizedStringResource(R.string.quiz_result_no_more_xp)
                                    state.passed -> localizedStringResource(R.string.quiz_result_completion_claimed)
                                    else -> localizedStringResource(R.string.quiz_result_no_xp)
                                },
                                color = LockedGrey
                            )
                        } else {
                            Text(
                                text = localizedStringResource(
                                    R.string.quiz_result_total_earned,
                                    state.xpEarned
                                ),
                                color = SunYellow
                            )
                        }
                    }
                },
                confirmButton = {
                    GamifiedButton(
                        text = localizedStringResource(R.string.common_finish),
                        onClick = {
                            viewModel.onFinishClicked()
                            onQuizFinished()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = ElectricCyan,
                        textColor = TrueBlack
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = ElectricCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(localizedStringResource(R.string.quiz_loading), color = TextLight)
                    Spacer(modifier = Modifier.weight(1f))
                }

                state.currentQuestion == null -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = state.loadError ?: localizedStringResource(R.string.lesson_message_unavailable),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = LockedGrey
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                else -> {
                    val question = state.currentQuestion
                    if (question != null) {
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = IceWhite
                        )

                        if (!question.codeSnippet.isNullOrBlank()) {
                            CodeSnippetBlock(
                                snippet = question.codeSnippet,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }

                        question.options.forEachIndexed { index, optionText ->
                            val buttonColor = getButtonColor(
                                isAnswered = state.isAnswered,
                                isCorrectAnswer = index == question.correctIndex,
                                isSelected = index == state.selectedIndex
                            )

                            val textColor = if (state.isAnswered && (index == question.correctIndex || index == state.selectedIndex)) {
                                TrueBlack
                            } else {
                                TextLight
                            }

                            GamifiedButton(
                                text = optionText,
                                onClick = { viewModel.submitAnswer(index) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                backgroundColor = buttonColor,
                                textColor = textColor,
                                enabled = !state.isAnswered
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.isAnswered) {
                            GamifiedButton(
                                text = localizedStringResource(R.string.quiz_next_question),
                                onClick = { viewModel.nextQuestion() },
                                backgroundColor = ElectricCyan,
                                textColor = TrueBlack
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.selectedIndex != question.correctIndex) {
                                Text(
                                    localizedStringResource(R.string.quiz_wrong_answer_lost_heart),
                                    color = CardinalRed
                                )
                            } else {
                                Text(
                                    localizedStringResource(R.string.quiz_correct_answer),
                                    color = LeafGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuizFeedbackEffects(state: QuizUiState) {
    val feedback = LocalAppFeedback.current
    val answeredKey = remember(state.currentIndex, state.isAnswered, state.selectedIndex) {
        if (!state.isAnswered || state.selectedIndex == null) {
            null
        } else {
            "${state.currentIndex}:${state.selectedIndex}"
        }
    }
    val completionKey = remember(state.finished, state.passed, state.perfect) {
        if (!state.finished || !state.passed) {
            null
        } else {
            "finished:${state.passed}:${state.perfect}"
        }
    }

    LaunchedEffect(answeredKey) {
        val selectedIndex = state.selectedIndex ?: return@LaunchedEffect
        if (!state.isAnswered) return@LaunchedEffect

        val currentQuestion = state.currentQuestion ?: return@LaunchedEffect
        if (selectedIndex == currentQuestion.correctIndex) {
            feedback.emit(FeedbackEvent.Success)
        } else {
            feedback.emit(FeedbackEvent.Error)
        }
    }

    LaunchedEffect(completionKey) {
        if (!state.finished || !state.passed) return@LaunchedEffect
        feedback.emit(FeedbackEvent.Reward)
    }
}

@Composable
fun getButtonColor(isAnswered: Boolean, isCorrectAnswer: Boolean, isSelected: Boolean): Color {
    return when {
        !isAnswered -> Charcoal
        isCorrectAnswer -> LeafGreen
        isSelected && !isCorrectAnswer -> CardinalRed
        else -> LockedGrey
    }
}

@Composable
private fun CodeSnippetBlock(
    snippet: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DeepCharcoal,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal)
    ) {
        SelectionContainer {
            Text(
                text = snippet.trim(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = ElectricCyan,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

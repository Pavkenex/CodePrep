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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun QuizScreen(viewModel: QuizViewModel = hiltViewModel(),
               onQuizFinished:()-> Unit
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "❤️ ${state.userHearts}", style = MaterialTheme.typography.titleLarge)
                Text(text = "Score: ${state.score}", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { paddingValues ->

        if (state.finished) {
            AlertDialog(
                onDismissRequest = { },
                title = {
                    Text(
                        text = "Kviz završen!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column {
                        val totalQuestions = state.questions.size
                        val correctAnswers = state.score
                        val percentage = if (totalQuestions > 0) {
                            (correctAnswers.toFloat() / totalQuestions * 100).toInt()
                        } else 0

                        Text("Vaš uspeh: $percentage%", style = MaterialTheme.typography.bodyLarge)
                        Text("Osvojeno XP: ${state.xpEarned} ✨", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tačnih odgovora: $correctAnswers / $totalQuestions")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onFinishClicked()
                            onQuizFinished()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Završi")
                    }
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
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Učitavanje pitanja...")
                    Spacer(modifier = Modifier.weight(1f))
                }

                state.currentQuestion == null -> {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = state.loadError ?: "Pitanja nisu dostupna.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                else -> {
                    val question = state.currentQuestion
                    if (question != null) {
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
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

                            Button(
                                onClick = {
                                    viewModel.submitAnswer(index)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                enabled = !state.isAnswered
                            ) {
                                Text(text = optionText)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.isAnswered) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Sledeće pitanje")
                            }

                            if (state.selectedIndex != question.correctIndex) {
                                Text("Pogrešno! Izgubili ste srce 💔", color = Color.Red)
                            } else {
                                Text("Tačno! +10 XP ✨", color = Color.Green)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getButtonColor(isAnswered: Boolean, isCorrectAnswer: Boolean, isSelected: Boolean): Color {
    return when {
        !isAnswered -> MaterialTheme.colorScheme.secondary
        isCorrectAnswer -> Color.Green
        isSelected && !isCorrectAnswer -> Color.Red
        else -> Color.Gray
    }
}

@Composable
private fun CodeSnippetBlock(
    snippet: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        SelectionContainer {
            Text(
                text = snippet.trim(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

package com.codeprep.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.domain.model.LessonContext
import com.codeprep.app.ui.ai.AskAiBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.theme.*

@Composable
fun LessonDetailScreen(
    viewModel: LessonViewModel = hiltViewModel(),
    onStartQuiz: (String) -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val courseTitle by viewModel.courseTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val quizBlockMessage by viewModel.quizBlockMessage.collectAsState()
    var showAiSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startQuizEvent.collectLatest { lessonId ->
            onStartQuiz(lessonId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        lesson?.let { l ->
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = l.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = l.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GamifiedButton(
                            text = "Započni kviz",
                            onClick = { viewModel.onStartQuizClicked() },
                            backgroundColor = ElectricCyan,
                            textColor = TrueBlack,
                            modifier = Modifier.weight(1f)
                        )

                        GamifiedButton(
                            text = "Pitaj AI",
                            onClick = { showAiSheet = true },
                            backgroundColor = Charcoal,
                            textColor = ElectricCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (quizBlockMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = quizBlockMessage.orEmpty(),
                            color = CardinalRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LaunchedEffect(quizBlockMessage) {
                            delay(2500)
                            viewModel.clearQuizBlockMessage()
                        }
                    }
                }
            }
        } ?: if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lekcija nije dostupna offline.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LockedGrey
                )
            }
        }

        if (showAiSheet) {
            lesson?.let { activeLesson ->
                AskAiBottomSheet(
                    lessonContext = LessonContext(
                        lessonId = activeLesson.lessonId,
                        courseTitle = courseTitle?.takeIf { it.isNotBlank() } ?: activeLesson.courseId,
                        lessonTitle = activeLesson.title,
                        theorySummary = activeLesson.content
                    ),
                    onDismiss = { showAiSheet = false }
                )
            }
        }
    }
}

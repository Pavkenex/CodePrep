package com.codeprep.app.ui.lesson

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun LessonListScreen(
    onLessonClick: (String) -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lessons = state.lessons

    if (lessons.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Nema dostupnih lekcija za izabrani modul.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Proveri internet konekciju i pokušaj ponovo.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(
                text = "Srce: ${state.hearts}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(lessons, key = { it.lesson.lessonId }) { lessonItem ->
            val lesson = lessonItem.lesson
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .alpha(if (lessonItem.canOpen) 1f else 0.65f)
                    .clickable(enabled = lessonItem.canOpen) { onLessonClick(lesson.lessonId) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        !lessonItem.isUnlocked -> Text(
                            text = "Zaključano: prethodna lekcija mora biti bez greške.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        lessonItem.isBlockedByHearts -> Text(
                            text = "Nemaš srca za novu lekciju trenutno.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        lessonItem.isPerfect -> Text(
                            text = "Završeno bez greške ✔",
                            style = MaterialTheme.typography.bodySmall
                        )
                        lessonItem.isCompleted -> Text(
                            text = "Završeno.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

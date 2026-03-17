package com.codeprep.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.data.local.CodeSnippet
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.domain.model.LessonContext
import com.codeprep.app.ui.ai.AskAiBottomSheet
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LessonDetailScreen(
    viewModel: LessonViewModel = hiltViewModel(),
    onStartQuiz: (String) -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val courseTitle by viewModel.courseTitle.collectAsState()
    val lessonProgress by viewModel.lessonProgress.collectAsState()
    val language by viewModel.selectedLanguage.collectAsState()
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
        lesson?.let { activeLesson ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    LessonHeroCard(
                        lesson = activeLesson,
                        courseTitle = courseTitle.orEmpty(),
                        progress = lessonProgress,
                        language = language
                    )
                }

                item {
                    SectionCard(
                        title = "Introduction",
                        body = activeLesson.content.introduction.resolve(language)
                    )
                }
                item {
                    SectionCard(
                        title = "Explanation",
                        body = activeLesson.content.explanation.resolve(language)
                    )
                }
                item {
                    SectionCard(
                        title = "Example",
                        body = activeLesson.content.example.resolve(language)
                    )
                }
                item {
                    HighlightCard(
                        title = "Key takeaway",
                        body = activeLesson.content.keyTakeaway.resolve(language)
                    )
                }

                activeLesson.analogy?.resolve(language)?.takeIf { it.isNotBlank() }?.let { analogy ->
                    item {
                        SectionCard(
                            title = "Analogy",
                            body = analogy
                        )
                    }
                }

                if (activeLesson.keyPoints.isNotEmpty()) {
                    item {
                        BulletSectionCard(
                            title = "Key points",
                            items = activeLesson.keyPoints.map { it.resolve(language) }
                        )
                    }
                }

                if (activeLesson.commonMistakes.isNotEmpty()) {
                    item {
                        BulletSectionCard(
                            title = "Common mistakes",
                            items = activeLesson.commonMistakes.map { it.resolve(language) },
                            accent = CardinalRed
                        )
                    }
                }

                if (activeLesson.codeSnippets.isNotEmpty()) {
                    items(activeLesson.codeSnippets, key = { snippet -> "${snippet.code.hashCode()}-${snippet.isAntiPattern}" }) { snippet ->
                        SnippetCard(snippet = snippet, language = language)
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GamifiedButton(
                                text = if (lessonProgress?.completed == true && lessonProgress?.perfectRun != true) {
                                    "Retry for star"
                                } else {
                                    "Start quiz"
                                },
                                onClick = { viewModel.onStartQuizClicked() },
                                backgroundColor = ElectricCyan,
                                textColor = TrueBlack,
                                modifier = Modifier.weight(1f)
                            )

                            GamifiedButton(
                                text = "Ask AI",
                                onClick = { showAiSheet = true },
                                backgroundColor = Charcoal,
                                textColor = ElectricCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (quizBlockMessage != null) {
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
                    text = "Lesson content is not available right now.",
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
                        lessonTitle = activeLesson.title.resolve(language),
                        theorySummary = viewModel.buildAiSummary(activeLesson, language)
                    ),
                    onDismiss = { showAiSheet = false }
                )
            }
        }
    }
}

@Composable
private fun LessonHeroCard(
    lesson: CachedLessonEntity,
    courseTitle: String,
    progress: LessonProgressEntity?,
    language: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Charcoal,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (courseTitle.isNotBlank()) {
                Text(
                    text = courseTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextLight
                )
            }
            Text(
                text = lesson.title.resolve(language),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = IceWhite
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroPill(text = "${lesson.xpReward} XP", accent = ElectricCyan)
                HeroPill(text = "${lesson.questionCount} questions", accent = LockedGrey)
                if (progress?.perfectRun == true) {
                    HeroPill(text = "Star earned", accent = SunYellow)
                } else if (progress?.completed == true) {
                    HeroPill(text = "Passed", accent = ElectricCyan)
                }
            }
            if (progress?.completed == true && progress.perfectRun != true) {
                Text(
                    text = "You passed this lesson. Retry the quiz with a perfect score to earn the star and bonus XP.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SunYellow
                )
            } else if (progress?.perfectRun == true) {
                Text(
                    text = "Perfect run earned. No more XP is available for this quiz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun HeroPill(text: String, accent: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    body: String
) {
    if (body.isBlank()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Charcoal,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ElectricCyan
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = IceWhite
            )
        }
    }
}

@Composable
private fun HighlightCard(
    title: String,
    body: String
) {
    if (body.isBlank()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = DeepCharcoal,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SunYellow
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = IceWhite
            )
        }
    }
}

@Composable
private fun BulletSectionCard(
    title: String,
    items: List<String>,
    accent: androidx.compose.ui.graphics.Color = ElectricCyan
) {
    val filteredItems = items.filter { it.isNotBlank() }
    if (filteredItems.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Charcoal,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent
            )
            filteredItems.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IceWhite
                )
            }
        }
    }
}

@Composable
private fun SnippetCard(
    snippet: CodeSnippet,
    language: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Charcoal,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (snippet.isAntiPattern) "Anti-pattern" else "Pseudocode",
                style = MaterialTheme.typography.labelLarge,
                color = if (snippet.isAntiPattern) CardinalRed else ElectricCyan
            )
            val description = snippet.description.resolve(language)
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IceWhite
                )
            }
            Surface(
                color = DeepCharcoal,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = snippet.code.trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

package com.codeprep.app.ui.lesson

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.runtime.saveable.rememberSaveable
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    LessonContentCard(
                        lesson = activeLesson,
                        language = language
                    )
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
private fun HeroPill(
    text: String,
    accent: androidx.compose.ui.graphics.Color
) {
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
private fun LessonContentCard(
    lesson: CachedLessonEntity,
    language: String
) {
    val introduction = lesson.content.introduction.resolve(language)
    val explanation = lesson.content.explanation.resolve(language)
    val example = lesson.content.example.resolve(language)
    val analogy = lesson.analogy?.resolve(language).orEmpty()
    val commonMistakes = lesson.commonMistakes
        .map { it.resolve(language) }
        .filter { it.isNotBlank() }
    val keyPoints = lesson.keyPoints
        .map { it.resolve(language) }
        .filter { it.isNotBlank() }
    val keyTakeaway = lesson.content.keyTakeaway.resolve(language)
    val exampleSnippets = lesson.codeSnippets.filterNot { it.isAntiPattern }
    val antiPatternSnippets = lesson.codeSnippets.filter { it.isAntiPattern }

    var isAnalogyExpanded by rememberSaveable(lesson.lessonId) { mutableStateOf(false) }
    var isAntiPatternExpanded by rememberSaveable(lesson.lessonId) { mutableStateOf(false) }
    var isMistakesExpanded by rememberSaveable(lesson.lessonId) { mutableStateOf(false) }
    var isSummaryExpanded by rememberSaveable(lesson.lessonId) { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Charcoal,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            LessonBodySection(
                title = "Introduction",
                body = introduction
            )

            LessonBodySection(
                title = "Explanation",
                body = explanation
            )

            if (analogy.isNotBlank()) {
                ExpandableLessonSection(
                    title = "Analogy",
                    accent = SunYellow,
                    expanded = isAnalogyExpanded,
                    onToggle = { isAnalogyExpanded = !isAnalogyExpanded }
                ) {
                    Text(
                        text = analogy,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IceWhite
                    )
                }
            }

            ExampleSection(
                body = example,
                snippets = exampleSnippets,
                language = language
            )

            if (antiPatternSnippets.isNotEmpty()) {
                ExpandableLessonSection(
                    title = "Anti-pattern",
                    accent = CardinalRed,
                    expanded = isAntiPatternExpanded,
                    onToggle = { isAntiPatternExpanded = !isAntiPatternExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        antiPatternSnippets.forEachIndexed { index, snippet ->
                            InlineSnippetBlock(
                                label = if (antiPatternSnippets.size > 1) "Anti-pattern ${index + 1}" else "Anti-pattern",
                                accent = CardinalRed,
                                snippet = snippet,
                                language = language,
                                containerColor = CardinalRed.copy(alpha = 0.08f),
                                borderColor = CardinalRed.copy(alpha = 0.22f),
                                codeColor = CardinalRed.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }

            if (commonMistakes.isNotEmpty()) {
                ExpandableLessonSection(
                    title = "Common mistakes",
                    accent = CardinalRed,
                    expanded = isMistakesExpanded,
                    onToggle = { isMistakesExpanded = !isMistakesExpanded }
                ) {
                    BulletList(
                        items = commonMistakes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (keyTakeaway.isNotBlank() || keyPoints.isNotEmpty()) {
                ExpandableLessonSection(
                    title = if (keyTakeaway.isNotBlank()) "Key takeaway" else "Key points",
                    accent = ElectricCyan,
                    expanded = isSummaryExpanded,
                    onToggle = { isSummaryExpanded = !isSummaryExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (keyTakeaway.isNotBlank()) {
                            Text(
                                text = keyTakeaway,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = IceWhite
                            )
                        }
                        if (keyPoints.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Key points",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan
                                )
                                BulletList(
                                    items = keyPoints,
                                    style = MaterialTheme.typography.bodyMedium
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
private fun LessonBodySection(
    title: String,
    body: String
) {
    if (body.isBlank()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = IceWhite
        )
    }
}

@Composable
private fun ExampleSection(
    body: String,
    snippets: List<CodeSnippet>,
    language: String
) {
    if (body.isBlank() && snippets.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Example",
            style = MaterialTheme.typography.labelLarge,
            color = ElectricCyan
        )

        if (body.isNotBlank()) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = IceWhite
            )
        }

        snippets.forEachIndexed { index, snippet ->
            InlineSnippetBlock(
                label = if (snippets.size > 1) "Pseudocode ${index + 1}" else "Pseudocode",
                accent = ElectricCyan,
                snippet = snippet,
                language = language
            )
        }
    }
}

@Composable
private fun ExpandableLessonSection(
    title: String,
    accent: androidx.compose.ui.graphics.Color,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
                )
                ExpansionBadge(
                    text = if (expanded) "-" else "+",
                    accent = accent
                )
            }
            if (expanded) {
                content()
            }
        }
    }
}

@Composable
private fun ExpansionBadge(
    text: String,
    accent: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            color = accent,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun BulletList(
    items: List<String>,
    style: androidx.compose.ui.text.TextStyle
) {
    val filteredItems = items.filter { it.isNotBlank() }
    if (filteredItems.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filteredItems.forEach { item ->
            Text(
                text = "• $item",
                style = style,
                color = IceWhite
            )
        }
    }
}

@Composable
private fun InlineSnippetBlock(
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    snippet: CodeSnippet,
    language: String,
    containerColor: androidx.compose.ui.graphics.Color = DeepCharcoal,
    borderColor: androidx.compose.ui.graphics.Color = ElectricCyan.copy(alpha = 0.12f),
    codeColor: androidx.compose.ui.graphics.Color = ElectricCyan
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = accent
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
                color = TrueBlack.copy(alpha = 0.28f),
                shape = RoundedCornerShape(14.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = snippet.code.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = codeColor,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp)
                    )
                }
            }
        }
    }
}

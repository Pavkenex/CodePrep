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
import androidx.compose.ui.unit.Dp
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
            val contentModel = remember(activeLesson, language) {
                LessonContentUiModel.from(activeLesson, language)
            }
            var isAnalogyExpanded by rememberSaveable(activeLesson.lessonId) { mutableStateOf(false) }
            var isAntiPatternExpanded by rememberSaveable(activeLesson.lessonId) { mutableStateOf(false) }
            var isMistakesExpanded by rememberSaveable(activeLesson.lessonId) { mutableStateOf(false) }
            var isSummaryExpanded by rememberSaveable(activeLesson.lessonId) { mutableStateOf(false) }
            val visibleSegments = remember(contentModel) {
                buildList {
                    if (contentModel.hasCoreSection) add(LessonContentSegment.Core)
                    if (contentModel.hasAnalogy) add(LessonContentSegment.Analogy)
                    if (contentModel.hasExampleSection) add(LessonContentSegment.Example)
                    if (contentModel.hasAntiPatterns) add(LessonContentSegment.AntiPattern)
                    if (contentModel.hasCommonMistakes) add(LessonContentSegment.CommonMistakes)
                    if (contentModel.hasSummary) add(LessonContentSegment.Summary)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    LessonHeroCard(
                        lesson = activeLesson,
                        courseTitle = courseTitle.orEmpty(),
                        progress = lessonProgress,
                        language = language
                    )
                }

                if (contentModel.hasCoreSection) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.Core)
                        ) {
                            if (contentModel.introduction.isNotBlank()) {
                                LessonBodySection(
                                    title = "Introduction",
                                    body = contentModel.introduction
                                )
                            }
                            if (contentModel.explanation.isNotBlank()) {
                                LessonBodySection(
                                    title = "Explanation",
                                    body = contentModel.explanation
                                )
                            }
                        }
                    }
                }

                if (contentModel.hasAnalogy) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.Analogy)
                        ) {
                            ExpandableLessonSection(
                                title = "Analogy",
                                accent = SunYellow,
                                expanded = isAnalogyExpanded,
                                onToggle = { isAnalogyExpanded = !isAnalogyExpanded }
                            ) {
                                Text(
                                    text = contentModel.analogy,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IceWhite
                                )
                            }
                        }
                    }
                }

                if (contentModel.hasExampleSection) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.Example)
                        ) {
                            ExampleSection(
                                body = contentModel.example,
                                snippets = contentModel.exampleSnippets,
                                language = language
                            )
                        }
                    }
                }

                if (contentModel.hasAntiPatterns) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.AntiPattern)
                        ) {
                            ExpandableLessonSection(
                                title = "Anti-pattern",
                                accent = CardinalRed,
                                expanded = isAntiPatternExpanded,
                                onToggle = { isAntiPatternExpanded = !isAntiPatternExpanded }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    contentModel.antiPatternSnippets.forEachIndexed { index, snippet ->
                                        InlineSnippetBlock(
                                            label = if (contentModel.antiPatternSnippets.size > 1) "Anti-pattern ${index + 1}" else "Anti-pattern",
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
                    }
                }

                if (contentModel.hasCommonMistakes) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.CommonMistakes)
                        ) {
                            ExpandableLessonSection(
                                title = "Common mistakes",
                                accent = CardinalRed,
                                expanded = isMistakesExpanded,
                                onToggle = { isMistakesExpanded = !isMistakesExpanded }
                            ) {
                                BulletList(
                                    items = contentModel.commonMistakes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                if (contentModel.hasSummary) {
                    item {
                        LessonCardSegment(
                            position = lessonCardSegmentPosition(visibleSegments, LessonContentSegment.Summary)
                        ) {
                            ExpandableLessonSection(
                                title = if (contentModel.keyTakeaway.isNotBlank()) "Key takeaway" else "Key points",
                                accent = ElectricCyan,
                                expanded = isSummaryExpanded,
                                onToggle = { isSummaryExpanded = !isSummaryExpanded }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (contentModel.keyTakeaway.isNotBlank()) {
                                        Text(
                                            text = contentModel.keyTakeaway,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = IceWhite
                                        )
                                    }
                                    if (contentModel.keyPoints.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Key points",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = ElectricCyan
                                            )
                                            BulletList(
                                                items = contentModel.keyPoints,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
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

private enum class LessonContentSegment {
    Core,
    Analogy,
    Example,
    AntiPattern,
    CommonMistakes,
    Summary
}

private enum class LessonCardSegmentPosition {
    Single,
    Top,
    Middle,
    Bottom
}

private data class LessonContentUiModel(
    val introduction: String,
    val explanation: String,
    val analogy: String,
    val example: String,
    val commonMistakes: List<String>,
    val keyPoints: List<String>,
    val keyTakeaway: String,
    val exampleSnippets: List<CodeSnippet>,
    val antiPatternSnippets: List<CodeSnippet>
) {
    val hasCoreSection: Boolean
        get() = introduction.isNotBlank() || explanation.isNotBlank()

    val hasAnalogy: Boolean
        get() = analogy.isNotBlank()

    val hasExampleSection: Boolean
        get() = example.isNotBlank() || exampleSnippets.isNotEmpty()

    val hasAntiPatterns: Boolean
        get() = antiPatternSnippets.isNotEmpty()

    val hasCommonMistakes: Boolean
        get() = commonMistakes.isNotEmpty()

    val hasSummary: Boolean
        get() = keyTakeaway.isNotBlank() || keyPoints.isNotEmpty()

    companion object {
        fun from(
            lesson: CachedLessonEntity,
            language: String
        ): LessonContentUiModel {
            return LessonContentUiModel(
                introduction = lesson.content.introduction.resolve(language),
                explanation = lesson.content.explanation.resolve(language),
                analogy = lesson.analogy?.resolve(language).orEmpty(),
                example = lesson.content.example.resolve(language),
                commonMistakes = lesson.commonMistakes
                    .map { it.resolve(language) }
                    .filter { it.isNotBlank() },
                keyPoints = lesson.keyPoints
                    .map { it.resolve(language) }
                    .filter { it.isNotBlank() },
                keyTakeaway = lesson.content.keyTakeaway.resolve(language),
                exampleSnippets = lesson.codeSnippets.filterNot { it.isAntiPattern },
                antiPatternSnippets = lesson.codeSnippets.filter { it.isAntiPattern }
            )
        }
    }
}

private fun lessonCardSegmentPosition(
    visibleSegments: List<LessonContentSegment>,
    currentSegment: LessonContentSegment
): LessonCardSegmentPosition {
    val index = visibleSegments.indexOf(currentSegment)
    if (index == -1) return LessonCardSegmentPosition.Single
    return when {
        visibleSegments.size == 1 -> LessonCardSegmentPosition.Single
        index == 0 -> LessonCardSegmentPosition.Top
        index == visibleSegments.lastIndex -> LessonCardSegmentPosition.Bottom
        else -> LessonCardSegmentPosition.Middle
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
private fun LessonCardSegment(
    position: LessonCardSegmentPosition,
    content: @Composable () -> Unit
) {
    val shape = when (position) {
        LessonCardSegmentPosition.Single -> RoundedCornerShape(28.dp)
        LessonCardSegmentPosition.Top -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        LessonCardSegmentPosition.Middle -> RoundedCornerShape(0.dp)
        LessonCardSegmentPosition.Bottom -> RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    }
    val topPadding: Dp = if (position == LessonCardSegmentPosition.Top || position == LessonCardSegmentPosition.Single) 20.dp else 18.dp
    val bottomPadding: Dp = if (position == LessonCardSegmentPosition.Bottom || position == LessonCardSegmentPosition.Single) 20.dp else 0.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Charcoal,
        shape = shape
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = topPadding, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            content()
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

package com.codeprep.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.R
import com.codeprep.app.data.local.entity.Question
import com.codeprep.app.ui.components.CodePrepCodeBlock
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.localization.localizedPluralStringResource
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppCodeTypography
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.LeafGreen
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack
import com.codeprep.app.ui.theme.White

internal data class HomeScreenLayoutSpec(
    val useSplitLayout: Boolean,
    val screenPaddingDp: Int,
    val tabletTopPaddingDp: Int,
    val verticalSpacingDp: Int,
    val maxContainerWidthDp: Int,
    val railWidthDp: Int,
    val contentWidthDp: Int,
    val columnGapDp: Int,
    val launchButtonWidthDp: Int
)

internal fun homeScreenLayoutFor(screenWidthDp: Int): HomeScreenLayoutSpec {
    if (screenWidthDp < 600) {
        return HomeScreenLayoutSpec(
            useSplitLayout = false,
            screenPaddingDp = 16,
            tabletTopPaddingDp = 0,
            verticalSpacingDp = 24,
            maxContainerWidthDp = 0,
            railWidthDp = 0,
            contentWidthDp = 0,
            columnGapDp = 0,
            launchButtonWidthDp = 0
        )
    }

    val screenPaddingDp = 40
    val tabletTopPaddingDp = 56
    val columnGapDp = 20
    val maxContainerWidthDp = (screenWidthDp - (screenPaddingDp * 2))
        .coerceAtLeast(0)
        .coerceAtMost(800)
    val railWidthDp = (((maxContainerWidthDp - columnGapDp) * 0.38f).toInt())
        .coerceAtLeast(230)
        .coerceAtMost(290)
    val contentWidthDp = maxContainerWidthDp - columnGapDp - railWidthDp

    return HomeScreenLayoutSpec(
        useSplitLayout = true,
        screenPaddingDp = screenPaddingDp,
        tabletTopPaddingDp = tabletTopPaddingDp,
        verticalSpacingDp = 24,
        maxContainerWidthDp = maxContainerWidthDp,
        railWidthDp = railWidthDp,
        contentWidthDp = contentWidthDp,
        columnGapDp = columnGapDp,
        launchButtonWidthDp = railWidthDp.coerceAtMost(220)
    )
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCoursesClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val layout = homeScreenLayoutFor(configuration.screenWidthDp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        layout = layout,
        onExpand = viewModel::expandDailyChallenge,
        onAnswer = viewModel::submitDailyAnswer,
        onComplete = viewModel::completeDailyChallenge,
        onCoursesClick = onCoursesClick
    )
}

@Composable
internal fun HomeScreenContent(
    uiState: HomeUiState,
    layout: HomeScreenLayoutSpec,
    onExpand: () -> Unit,
    onAnswer: (Int) -> Unit,
    onComplete: () -> Unit,
    onCoursesClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = layout.screenPaddingDp.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(layout.verticalSpacingDp.dp)
    ) {
        if (layout.useSplitLayout) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = layout.tabletTopPaddingDp.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier.width(layout.maxContainerWidthDp.dp),
                    horizontalArrangement = Arrangement.spacedBy(layout.columnGapDp.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    HomeSupportingRail(
                        uiState = uiState,
                        launchButtonWidthDp = layout.launchButtonWidthDp,
                        onCoursesClick = onCoursesClick,
                        modifier = Modifier
                            .width(layout.railWidthDp.dp)
                            .testTag("home-supporting-rail")
                    )
                    HomePrimaryContent(
                        uiState = uiState,
                        onExpand = onExpand,
                        onAnswer = onAnswer,
                        onComplete = onComplete,
                        modifier = Modifier
                            .width(layout.contentWidthDp.dp)
                            .testTag("home-primary-content")
                    )
                }
            }
        } else {
            HeaderSection(nickname = uiState.nickname)
            SystemUptimeSection(days = uiState.streak)
            DailyChallengeWidget(
                dailyState = uiState.dailyChallenge,
                onExpand = onExpand,
                onAnswer = onAnswer,
                onComplete = onComplete
            )
            SystemStatusWidget(
                level = uiState.level,
                currentXp = uiState.currentLevelXp,
                xpRequired = uiState.xpRequiredForNextLevel
            )
            LaunchModulesButton(onClick = onCoursesClick)
        }
    }
}

@Composable
private fun HomeSupportingRail(
    uiState: HomeUiState,
    launchButtonWidthDp: Int,
    onCoursesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HeaderSection(nickname = uiState.nickname)
        SystemUptimeSection(days = uiState.streak)
        SystemStatusWidget(
            level = uiState.level,
            currentXp = uiState.currentLevelXp,
            xpRequired = uiState.xpRequiredForNextLevel
        )
        LaunchModulesButton(
            onClick = onCoursesClick,
            modifier = Modifier
                .testTag("home-launch-modules-button")
                .align(Alignment.CenterHorizontally)
                .then(
                    if (launchButtonWidthDp > 0) {
                        Modifier.widthIn(max = launchButtonWidthDp.dp)
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@Composable
private fun HomePrimaryContent(
    uiState: HomeUiState,
    onExpand: () -> Unit,
    onAnswer: (Int) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DailyChallengeWidget(
            dailyState = uiState.dailyChallenge,
            onExpand = onExpand,
            onAnswer = onAnswer,
            onComplete = onComplete
        )
    }
}

@Composable
fun HeaderSection(nickname: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Charcoal)
                .border(2.dp, ElectricCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = localizedStringResource(R.string.home_user_avatar),
                tint = ElectricCyan,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = localizedStringResource(R.string.home_welcome_back),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = nickname,
                color = ElectricCyan,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun SystemUptimeSection(days: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = localizedStringResource(R.string.home_uptime_title),
            color = ElectricCyan,
            style = AppCodeTypography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Charcoal),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = localizedPluralStringResource(R.plurals.home_uptime_days, days, days),
                        color = White,
                        style = AppCodeTypography.headlineLarge
                    )
                    Text(
                        text = localizedStringResource(R.string.home_uptime_status),
                        color = Color.Green,
                        style = AppCodeTypography.labelSmall
                    )
                }
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = localizedStringResource(R.string.home_streak_icon),
                    tint = ElectricCyan,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}


@Composable
fun DailyChallengeWidget(
    dailyState: DailyChallengeUiState,
    onExpand: () -> Unit,
    onAnswer: (Int) -> Unit,
    onComplete: () -> Unit
) {
    val question = dailyState.currentQuestion
    val canExpand = question != null && !dailyState.isCompleted && !dailyState.isExpanded

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canExpand) {
                    Modifier.clickable(onClick = onExpand)
                } else {
                    Modifier
                }
            )
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(2.dp, ElectricCyan)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = localizedStringResource(R.string.home_daily_challenge_icon),
                    tint = CardinalRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedStringResource(R.string.home_daily_challenge_title),
                    color = CardinalRed,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                dailyState.isLoading -> {
                    Text(
                        text = localizedStringResource(R.string.home_daily_challenge_loading),
                        color = White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = localizedStringResource(R.string.home_daily_challenge_syncing),
                        color = ElectricCyan,
                        style = AppCodeTypography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                dailyState.isCompleted -> {
                    Text(
                        text = localizedStringResource(R.string.home_daily_challenge_completed_title),
                        color = LeafGreen,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizedStringResource(R.string.home_daily_challenge_completed_body),
                        color = White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                question == null -> {
                    Text(
                        text = dailyState.error ?: localizedStringResource(R.string.home_daily_challenge_unavailable),
                        color = White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                !dailyState.isExpanded -> {
                    DailyChallengePreview(
                        question = question,
                        onExpand = onExpand
                    )
                }

                else -> {
                    DailyChallengeQuestionContent(
                        question = question,
                        selectedIndex = dailyState.selectedIndex,
                        isAnswered = dailyState.isAnswered,
                        onAnswer = onAnswer,
                        onComplete = onComplete
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyChallengePreview(
    question: Question,
    onExpand: () -> Unit
) {
    if (question.title.isNotBlank()) {
        Text(
            text = question.title,
            color = White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    QuestionMetaRow(question = question)
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = question.text,
        color = White,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 3
    )
    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = localizedStringResource(R.string.home_daily_challenge_expand_hint),
        color = TextLight,
        style = MaterialTheme.typography.bodyMedium
    )
    Text(
        text = localizedStringResource(R.string.home_daily_challenge_opening),
        color = ElectricCyan,
        style = AppCodeTypography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp)
    )


    Spacer(modifier = Modifier.height(12.dp))

    GamifiedButton(
        text = localizedStringResource(R.string.home_daily_challenge_open_button),
        onClick = onExpand,
        backgroundColor = ElectricCyan,
        textColor = TrueBlack,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun DailyChallengeQuestionContent(
    question: Question,
    selectedIndex: Int?,
    isAnswered: Boolean,
    onAnswer: (Int) -> Unit,
    onComplete: () -> Unit
) {
    if (question.title.isNotBlank()) {
        Text(
            text = question.title,
            color = White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    QuestionMetaRow(question = question)
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = question.text,
        color = White,
        style = MaterialTheme.typography.bodyLarge
    )

    if (!question.codeSnippet.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(16.dp))
        DailyChallengeCodeSnippet(
            snippet = question.codeSnippet,
            language = question.codeSnippetLanguage
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    question.options.forEachIndexed { index, option ->
        GamifiedButton(
            text = option,
            onClick = { onAnswer(index) },
            enabled = !isAnswered,
            backgroundColor = dailyOptionColor(
                isAnswered = isAnswered,
                isCorrectAnswer = index == question.correctIndex,
                isSelected = index == selectedIndex
            ),
            textColor = if (isAnswered && (index == question.correctIndex || index == selectedIndex)) {
                TrueBlack
            } else {
                White
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }

    if (isAnswered) {
        Spacer(modifier = Modifier.height(16.dp))

        val isCorrect = selectedIndex == question.correctIndex
        Text(
            text = if (isCorrect) {
                localizedStringResource(R.string.home_answer_correct)
            } else {
                localizedStringResource(R.string.home_answer_incorrect)
            },
            color = if (isCorrect) LeafGreen else CardinalRed,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (question.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.explanation,
                color = White,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GamifiedButton(
            text = localizedStringResource(R.string.home_daily_challenge_complete_button),
            onClick = onComplete,
            backgroundColor = ElectricCyan,
            textColor = TrueBlack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuestionMetaRow(question: Question) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (question.difficulty.isNotBlank()) {
            ChallengeBadge(
                label = question.difficulty.prettyLabel(),
                color = SunYellow
            )
        }
        if (question.type.isNotBlank()) {
            ChallengeBadge(
                label = question.type.prettyLabel(),
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun ChallengeBadge(
    label: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            color = color,
            style = AppCodeTypography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DailyChallengeCodeSnippet(snippet: String) {
    DailyChallengeCodeSnippet(
        snippet = snippet,
        language = null
    )
}

@Composable
private fun DailyChallengeCodeSnippet(
    snippet: String,
    language: String?
) {
    CodePrepCodeBlock(
        code = snippet.trim(),
        language = language,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SystemStatusWidget(
    level: Int,
    currentXp: Int,
    xpRequired: Int
) {
    val progress = if (xpRequired == 0) 0f else currentXp / xpRequired.toFloat()

    Card(
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = localizedStringResource(R.string.home_status_title),
                color = Color.Gray,
                style = AppCodeTypography.labelSmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedStringResource(R.string.home_status_level, level),
                    color = White,
                    style = AppCodeTypography.titleMedium
                )
                Text(
                    text = localizedStringResource(R.string.home_status_xp, currentXp, xpRequired),
                    color = ElectricCyan,
                    style = AppCodeTypography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ElectricCyan,
                trackColor = Color.DarkGray,
            )
        }
    }
}

@Composable
fun LaunchModulesButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = localizedStringResource(R.string.home_launch_modules_icon),
            tint = TrueBlack
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = localizedStringResource(R.string.home_launch_modules),
            color = TrueBlack,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                color = TrueBlack
            )
        )
    }
}

private fun dailyOptionColor(
    isAnswered: Boolean,
    isCorrectAnswer: Boolean,
    isSelected: Boolean
): Color {
    return when {
        !isAnswered -> DeepCharcoal
        isCorrectAnswer -> LeafGreen
        isSelected && !isCorrectAnswer -> CardinalRed
        else -> LockedGrey
    }
}

private fun String.prettyLabel(): String {
    return replace('_', ' ')
        .split(' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.titlecase() }
        }
}

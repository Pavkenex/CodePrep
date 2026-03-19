package com.codeprep.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.R
import com.codeprep.app.data.local.entity.Question
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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCoursesClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HeaderSection(nickname = uiState.nickname)
        SystemUptimeSection(days = uiState.streak)
        DailyChallengeWidget(
            dailyState = uiState.dailyChallenge,
            onExpand = viewModel::expandDailyChallenge,
            onAnswer = viewModel::submitDailyAnswer,
            onComplete = viewModel::completeDailyChallenge
        )
        SystemStatusWidget(
            level = uiState.level,
            currentXp = uiState.currentLevelXp,
            xpRequired = uiState.xpRequiredForNextLevel
        )
        LaunchModulesButton(onClick = onCoursesClick)
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
        DailyChallengeCodeSnippet(snippet = question.codeSnippet)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepCharcoal,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Charcoal)
    ) {
        SelectionContainer {
            Text(
                text = snippet.trim(),
                style = AppCodeTypography.bodySmall,
                color = ElectricCyan,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
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
fun LaunchModulesButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
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

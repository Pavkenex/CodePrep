package com.codeprep.app.ui.ai

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.R
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.TextLight

@Composable
fun AskAiScreen(
    onLessonClick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AskAiViewModel = hiltViewModel()
) {
    val explanationsUiState by viewModel.explanationsUiState.collectAsState()
    val languageCode by viewModel.selectedLanguage.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    var expandedCourseId by rememberSaveable { mutableStateOf<String?>(null) }
    var lessonPendingDeletion by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(explanationsUiState.modules) {
        if (explanationsUiState.modules.none { it.courseId == expandedCourseId }) {
            expandedCourseId = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "explanations-header") {
            Column {
                Text(
                    text = localizedAiString(languageCode, R.string.explanations_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = IceWhite
                )
                Text(
                    text = localizedAiString(languageCode, R.string.explanations_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LockedGrey,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Locked banner: saved explanations stay browsable, only new questions
        // are gated (the tutor itself is opened from a lesson).
        if (!hasApiKey) {
            item(key = "explanations-locked") {
                AskAiLockedCard(
                    languageCode = languageCode,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        when {
            explanationsUiState.isLoading -> {
                item(key = "explanations-loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ElectricCyan)
                    }
                }
            }

            explanationsUiState.isEmpty -> {
                item(key = "explanations-empty") {
                    ExplanationsEmptyState(
                        languageCode = languageCode,
                        modifier = Modifier.fillParentMaxHeight(0.6f)
                    )
                }
            }

            else -> {
                items(explanationsUiState.modules, key = { it.courseId }) { module ->
                    ExplanationsModuleCard(
                        module = module,
                        expanded = expandedCourseId == module.courseId,
                        onToggle = {
                            expandedCourseId = if (expandedCourseId == module.courseId) {
                                null
                            } else {
                                module.courseId
                            }
                        },
                        onLessonClick = onLessonClick,
                        onDeleteClick = { lessonPendingDeletion = it }
                    )
                }
            }
        }
    }

    lessonPendingDeletion?.let { lessonId ->
        AlertDialog(
            onDismissRequest = { lessonPendingDeletion = null },
            title = {
                Text(
                    text = localizedAiString(languageCode, R.string.explanations_delete_title),
                    color = IceWhite
                )
            },
            text = {
                Text(
                    text = localizedAiString(languageCode, R.string.explanations_delete_body),
                    color = TextLight
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSavedConversation(lessonId)
                        lessonPendingDeletion = null
                    }
                ) {
                    Text(
                        text = localizedAiString(languageCode, R.string.common_delete),
                        color = ElectricCyan
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { lessonPendingDeletion = null }) {
                    Text(
                        text = localizedAiString(languageCode, R.string.common_cancel),
                        color = LockedGrey
                    )
                }
            },
            containerColor = Charcoal
        )
    }
}

@Composable
private fun ExplanationsEmptyState(
    languageCode: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Charcoal,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = localizedAiString(languageCode, R.string.explanations_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                color = IceWhite
            )
            Text(
                text = localizedAiString(languageCode, R.string.explanations_empty_body),
                style = MaterialTheme.typography.bodyLarge,
                color = TextLight,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun ExplanationsModuleCard(
    module: ExplanationsModuleUi,
    expanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Charcoal,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = IceWhite,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ElectricCyan
                )
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    module.lessons.forEach { lesson ->
                        ExplanationsLessonRow(
                            lesson = lesson,
                            onClick = { onLessonClick(lesson.lessonId) },
                            onDeleteClick = { onDeleteClick(lesson.lessonId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExplanationsLessonRow(
    lesson: ExplanationLessonUi,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepCharcoal,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, IceWhite.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onClick),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IceWhite
                )
                if (lesson.preview.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(ElectricCyan.copy(alpha = 0.06f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ElectricCyan.copy(alpha = 0.55f))
                                .padding(horizontal = 1.5.dp, vertical = 16.dp)
                        )
                        Text(
                            text = "\"${lesson.preview}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextLight,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = LockedGrey
                )
            }
        }
    }
}

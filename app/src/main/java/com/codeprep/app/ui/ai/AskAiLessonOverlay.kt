package com.codeprep.app.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.R
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.domain.model.LessonContext
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

@Composable
fun AskAiLessonOverlay(
    lessonContext: LessonContext,
    languageCode: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: AskAiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var question by rememberSaveable(lessonContext.lessonId) { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(lessonContext.lessonId) {
        viewModel.bindLesson(lessonContext)
    }

    LaunchedEffect(uiState.messages.size, uiState.isLoading, visible) {
        if (!visible) return@LaunchedEffect
        val loadingOffset = if (uiState.isLoading) 1 else 0
        val lastIndex = uiState.messages.lastIndex + loadingOffset
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    BackHandler(enabled = visible, onBack = onDismiss)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(180)) + scaleIn(
            animationSpec = tween(260),
            initialScale = 0.72f,
            transformOrigin = TransformOrigin(0.84f, 0.96f)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            animationSpec = tween(220),
            targetScale = 0.84f,
            transformOrigin = TransformOrigin(0.84f, 0.96f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TrueBlack.copy(alpha = 0.46f))
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    AskAiHeader(
                        languageCode = languageCode,
                        uiState = uiState,
                        onDismiss = onDismiss,
                        onSave = viewModel::saveConversation
                    )

                    if (uiState.messages.isEmpty()) {
                        AskAiEmptyState(
                            modifier = Modifier.weight(1f),
                            lessonContext = lessonContext,
                            languageCode = languageCode,
                            onPrompt = { prompt ->
                                question = prompt
                            }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.messages, key = { it.id }) { message ->
                                ConversationBubble(message = message)
                            }
                            if (uiState.isLoading) {
                                item("loading-indicator") {
                                    LoadingBubble(languageCode = languageCode)
                                }
                            }
                        }
                    }

                    AskAiComposer(
                        languageCode = languageCode,
                        question = question,
                        onQuestionChange = { question = it },
                        onSend = {
                            viewModel.ask(question)
                            question = ""
                        },
                        enabled = !uiState.isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun AskAiHeader(
    languageCode: String,
    uiState: AskAiUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp)) {
            Text(localizedAiString(languageCode, R.string.ask_ai_back), color = ElectricCyan)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.lessonTitle.ifBlank {
                    localizedAiString(languageCode, R.string.ask_ai_title_fallback)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = IceWhite
            )
            Text(
                text = uiState.courseTitle.ifBlank {
                    localizedAiString(languageCode, R.string.ask_ai_course_fallback)
                },
                style = MaterialTheme.typography.bodySmall,
                color = LockedGrey
            )
        }

        FilledTonalButton(
            onClick = onSave,
            enabled = uiState.canSave,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (uiState.isSaved) {
                    localizedAiString(languageCode, R.string.ask_ai_saved)
                } else {
                    localizedAiString(languageCode, R.string.ask_ai_save)
                },
                color = if (uiState.isSaved) SunYellow else ElectricCyan
            )
        }
    }
}

@Composable
private fun AskAiEmptyState(
    modifier: Modifier = Modifier,
    lessonContext: LessonContext,
    languageCode: String,
    onPrompt: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Charcoal,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = localizedAiString(languageCode, R.string.ask_ai_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = IceWhite
                )
                Text(
                    text = localizedAiString(
                        languageCode,
                        R.string.ask_ai_empty_body,
                        lessonContext.lessonTitle
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight
                )
                QuickPromptRow(
                    languageCode = languageCode,
                    onPrompt = onPrompt
                )
            }
        }
    }
}

@Composable
private fun QuickPromptRow(
    languageCode: String,
    onPrompt: (String) -> Unit
) {
    val prompts = listOf(
        localizedAiString(languageCode, R.string.ask_ai_prompt_simpler),
        localizedAiString(languageCode, R.string.ask_ai_prompt_example),
        localizedAiString(languageCode, R.string.ask_ai_prompt_quiz)
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        prompts.forEach { prompt ->
            AssistChip(
                onClick = { onPrompt(prompt) },
                label = {
                    Text(prompt, color = IceWhite)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = DeepCharcoal,
                    labelColor = IceWhite
                ),
                border = null
            )
        }
    }
}

@Composable
private fun AskAiComposer(
    languageCode: String,
    question: String,
    onQuestionChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = question,
            onValueChange = onQuestionChange,
            label = {
                Text(localizedAiString(languageCode, R.string.ask_ai_input_label), color = TextLight)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp, max = 144.dp),
            minLines = 2,
            maxLines = 4
        )

        GamifiedButton(
            text = if (enabled) {
                localizedAiString(languageCode, R.string.ask_ai_send)
            } else {
                localizedAiString(languageCode, R.string.ask_ai_thinking)
            },
            onClick = onSend,
            enabled = enabled && question.isNotBlank(),
            backgroundColor = ElectricCyan,
            textColor = TrueBlack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConversationBubble(message: AiConversationMessage) {
    val isUser = message.role == AiConversationRole.User
    val isSystem = message.role == AiConversationRole.System
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = when {
        isUser -> ElectricCyan
        isSystem -> CardinalRed.copy(alpha = 0.14f)
        else -> Charcoal
    }
    val textColor = when {
        isUser -> TrueBlack
        isSystem -> IceWhite
        else -> IceWhite
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (isSystem) 1f else 0.88f),
            color = containerColor,
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = if (isUser) 22.dp else 8.dp,
                bottomEnd = if (isUser) 8.dp else 22.dp
            )
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.padding(16.dp),
                textAlign = if (isSystem) TextAlign.Center else TextAlign.Start
            )
        }
    }
}

@Composable
private fun LoadingBubble(
    languageCode: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Charcoal,
            shape = RoundedCornerShape(22.dp, 22.dp, 22.dp, 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = ElectricCyan
                )
                Text(
                    text = localizedAiString(languageCode, R.string.ask_ai_loading_placeholder),
                    color = IceWhite
                )
            }
        }
    }
}

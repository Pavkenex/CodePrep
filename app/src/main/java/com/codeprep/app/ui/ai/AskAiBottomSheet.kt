package com.codeprep.app.ui.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.data.remote.api.AiConfig
import com.codeprep.app.domain.model.AiResponse
import com.codeprep.app.domain.model.LessonContext

import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAiBottomSheet(
    lessonContext: LessonContext,
    onDismiss: () -> Unit,
    viewModel: AskAiViewModel = hiltViewModel()
) {
    var question by remember { mutableStateOf("") }
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clearAnswer()
            onDismiss()
        },
        containerColor = AppBackground,
        contentColor = TextLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Pitaj AI o lekciji",
                style = MaterialTheme.typography.titleMedium,
                color = ElectricCyan
            )
            Text(
                text = "${lessonContext.courseTitle} > ${lessonContext.lessonTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = LockedGrey
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Tvoje pitanje...", color = TextLight) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = Charcoal,
                    cursorColor = ElectricCyan,
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = Charcoal,
                    unfocusedContainerColor = Charcoal
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            GamifiedButton(
                text = "Pitaj",
                onClick = { viewModel.ask(question, lessonContext) },
                backgroundColor = ElectricCyan,
                textColor = TrueBlack,
                enabled = question.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("AI razmišlja...", color = TextLight)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AiResponseContent(response = answer)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AiResponseContent(response: AiResponse?) {
    when (response) {
        is AiResponse.Success -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Charcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (response.fromCache) {
                        Text(
                            text = "Iz keša",
                            style = MaterialTheme.typography.labelSmall,
                            color = LockedGrey
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(response.answer, style = MaterialTheme.typography.bodyMedium, color = TextLight)
                }
            }
        }

        is AiResponse.Fallback -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LockedGreyDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AI nije dostupan. Evo sažetka lekcije:",
                        style = MaterialTheme.typography.labelLarge,
                        color = SunYellow
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = response.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight
                    )
                }
            }
        }

        is AiResponse.Error -> {
            Text(
                text = response.message,
                style = MaterialTheme.typography.bodyMedium,
                color = CardinalRed
            )
        }

        AiResponse.RateLimited -> {
            Text(
                text = "Dostignut je dnevni limit (${AiConfig.MAX_QUESTIONS_PER_DAY} pitanja).",
                style = MaterialTheme.typography.bodyMedium,
                color = CardinalRed
            )
        }

        null -> Unit
    }
}

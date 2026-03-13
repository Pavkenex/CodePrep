package com.codeprep.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.compose.foundation.background
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.theme.*

@Composable
fun AskAiScreen(
    viewModel: AskAiViewModel = hiltViewModel()
) {
    var question by remember { mutableStateOf("") }
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val history by viewModel.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Pitaj AI",
            style = MaterialTheme.typography.headlineSmall,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Postavi pitanje o programiranju...", color = TextLight) },
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
        Spacer(modifier = Modifier.height(10.dp))

        GamifiedButton(
            text = "Pitaj",
            onClick = {
                viewModel.ask(question, null)
                question = ""
            },
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
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Prethodna pitanja",
            style = MaterialTheme.typography.titleMedium,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (history.isEmpty()) {
                item {
                    Text(
                        text = "Još nema sačuvane istorije pitanja.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LockedGrey
                    )
                }
            } else {
                items(history, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Charcoal)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.question,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.answer,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = TextLight.copy(alpha = 0.8f)
                            )
                            if (!item.contextLessonId.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kontekst lekcije: ${item.contextLessonId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LockedGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

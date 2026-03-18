package com.codeprep.app.ui.ai

import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.R
import com.codeprep.app.domain.model.SavedAiConversationSummary
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import java.text.DateFormat
import java.util.Date

@Composable
fun AskAiScreen(
    viewModel: AskAiViewModel = hiltViewModel()
) {
    val savedConversations by viewModel.savedConversations.collectAsState()
    val languageCode by viewModel.selectedLanguage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        Text(
            text = localizedAiString(languageCode, R.string.ask_ai_saved_conversations_title),
            style = MaterialTheme.typography.headlineSmall,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = localizedAiString(languageCode, R.string.ask_ai_saved_conversations_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LockedGrey
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (savedConversations.isEmpty()) {
                item {
                    Text(
                        text = localizedAiString(languageCode, R.string.ask_ai_saved_conversations_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = LockedGrey
                    )
                }
            } else {
                items(savedConversations, key = { it.id }) { summary ->
                    SavedConversationCard(
                        languageCode = languageCode,
                        summary = summary
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedConversationCard(
    languageCode: String,
    summary: SavedAiConversationSummary
) {
    val formatter = rememberDateFormatter()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Charcoal)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = summary.lessonTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = IceWhite
            )
            Text(
                text = summary.courseTitle,
                style = MaterialTheme.typography.bodySmall,
                color = ElectricCyan
            )
            if (summary.preview.isNotBlank()) {
                Text(
                    text = summary.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IceWhite.copy(alpha = 0.85f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = localizedAiString(
                    languageCode,
                    R.string.ask_ai_saved_conversations_meta,
                    summary.messageCount,
                    formatter.format(Date(summary.updatedAt))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = LockedGrey
            )
        }
    }
}

@Composable
private fun rememberDateFormatter(): DateFormat {
    return androidx.compose.runtime.remember {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    }
}

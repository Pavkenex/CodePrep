package com.codeprep.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.R
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.components.GamifiedTextField
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack
import kotlinx.coroutines.launch

@Composable
fun AiSettingsSection(
    viewModel: AiSettingsViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    val savedApiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val savedModelId by viewModel.modelId.collectAsStateWithLifecycle()
    val savedBaseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testResult by viewModel.connectionTestResult.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Drafts are captured once when the section opens so typing never fights the
    // store flow; a fresh expansion picks up the latest saved values.
    var apiKeyDraft by rememberSaveable { mutableStateOf(savedApiKey) }
    var modelDraft by rememberSaveable { mutableStateOf(savedModelId) }
    var baseUrlDraft by rememberSaveable { mutableStateOf(savedBaseUrl) }
    var isKeyVisible by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = localizedStringResource(R.string.profile_ai_api_key_label),
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GamifiedTextField(
                    value = apiKeyDraft,
                    onValueChange = { apiKeyDraft = it },
                    placeholder = localizedStringResource(R.string.profile_ai_api_key_placeholder),
                    visualTransformation = if (isKeyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    backgroundColor = DeepCharcoal,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                    Icon(
                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = localizedStringResource(
                            if (isKeyVisible) R.string.profile_ai_hide_key else R.string.profile_ai_show_key
                        ),
                        tint = LockedGrey
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = localizedStringResource(R.string.profile_ai_model_label),
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
            GamifiedTextField(
                value = modelDraft,
                onValueChange = { modelDraft = it },
                placeholder = localizedStringResource(R.string.profile_ai_model_placeholder),
                backgroundColor = DeepCharcoal
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = localizedStringResource(R.string.profile_ai_base_url_label),
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
            GamifiedTextField(
                value = baseUrlDraft,
                onValueChange = { baseUrlDraft = it },
                placeholder = localizedStringResource(R.string.profile_ai_base_url_placeholder),
                backgroundColor = DeepCharcoal
            )
        }

        GamifiedButton(
            text = localizedStringResource(R.string.profile_ai_test_connection),
            onClick = {
                scope.launch {
                    viewModel.testConnection(apiKeyDraft, modelDraft, baseUrlDraft)
                }
            },
            enabled = !isTesting,
            backgroundColor = DeepCharcoal,
            textColor = IceWhite,
            modifier = Modifier.fillMaxWidth()
        )

        if (isTesting) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 6.dp),
                    strokeWidth = 2.dp,
                    color = ElectricCyan
                )
            }
        }

        testResult?.let { result ->
            Text(
                text = localizedStringResource(result.messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.isError) CardinalRed else ElectricCyan
            )
        }

        GamifiedButton(
            text = localizedStringResource(R.string.profile_ai_save),
            onClick = {
                viewModel.save(apiKeyDraft, modelDraft, baseUrlDraft)
                onSaved()
            },
            backgroundColor = ElectricCyan,
            textColor = TrueBlack,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = localizedStringResource(R.string.profile_ai_storage_note),
            style = MaterialTheme.typography.bodySmall,
            color = TextLight
        )
    }
}

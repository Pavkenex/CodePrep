package com.codeprep.app.ui.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.TextLight

/**
 * Shown in the Ask AI surfaces when no API key is configured. Explains why the
 * tutor is locked and offers a shortcut into Settings (AI section).
 */
@Composable
fun AskAiLockedCard(
    languageCode: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Charcoal,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = localizedAiString(languageCode, R.string.ai_key_required_title),
                style = MaterialTheme.typography.headlineSmall,
                color = IceWhite
            )
            Text(
                text = localizedAiString(languageCode, R.string.ai_key_required_body),
                style = MaterialTheme.typography.bodyLarge,
                color = TextLight
            )
            Text(
                text = localizedAiString(languageCode, R.string.ai_key_required_open_settings),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                ),
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .padding(vertical = 4.dp)
            )
        }
    }
}

package com.codeprep.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.TextLight

@Composable
fun FeedbackSettingsSection(
    soundEffectsEnabled: Boolean,
    hapticsEnabled: Boolean,
    onSoundEffectsToggle: () -> Unit,
    onHapticsToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FeedbackToggleOption(
            title = localizedStringResource(R.string.profile_feedback_sound_effects),
            subtitle = localizedStringResource(R.string.profile_feedback_sound_effects_subtitle),
            checked = soundEffectsEnabled,
            optionTag = "feedback_sound_option",
            onClick = onSoundEffectsToggle
        )
        FeedbackToggleOption(
            title = localizedStringResource(R.string.profile_feedback_haptics),
            subtitle = localizedStringResource(R.string.profile_feedback_haptics_subtitle),
            checked = hapticsEnabled,
            optionTag = "feedback_haptics_option",
            onClick = onHapticsToggle
        )
    }
}

@Composable
private fun FeedbackToggleOption(
    title: String,
    subtitle: String,
    checked: Boolean,
    optionTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Charcoal, RoundedCornerShape(16.dp))
            .testTag(optionTag)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = { onClick() }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = IceWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextLight
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier
        )
    }
}

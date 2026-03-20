package com.codeprep.app.ui.profile

enum class ProfileSettingsSection {
    Language,
    Feedback,
    Conversations
}

fun toggleSettingsSection(
    current: ProfileSettingsSection?,
    requested: ProfileSettingsSection
): ProfileSettingsSection? {
    return if (current == requested) {
        null
    } else {
        requested
    }
}

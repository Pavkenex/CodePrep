package com.codeprep.app.data.settings

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * One-shot deep link into a specific settings section. A surface (e.g. the
 * locked Ask AI card) requests an action, then navigates to the Profile tab;
 * [ProfileScreen] consumes the pending action on composition and the holder
 * clears so settings never re-open later.
 */
@Singleton
class PendingSettingsActionHolder @Inject constructor() {
    private val _pendingAction = MutableStateFlow<PendingSettingsAction?>(null)
    val pendingAction: StateFlow<PendingSettingsAction?> = _pendingAction.asStateFlow()

    fun requestAiSettings() {
        _pendingAction.value = PendingSettingsAction(PendingSettingsAction.AI_SETTINGS)
    }

    /** Returns and clears the pending action, or null when nothing is pending. */
    fun consume(): PendingSettingsAction? {
        val current = _pendingAction.value
        if (current != null) {
            _pendingAction.value = null
        }
        return current
    }
}

data class PendingSettingsAction(val section: String) {
    companion object {
        const val AI_SETTINGS = "ai_settings"
    }
}

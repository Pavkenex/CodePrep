package com.codeprep.app.ui.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the lesson AI chat overlay is on screen so the app shell can
 * hide its top/bottom chrome on short (landscape phone) viewports. Portrait and
 * tablet layouts keep the chrome because the overlay has enough room there.
 */
@Singleton
class AskAiOverlayVisibility @Inject constructor() {
    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    fun setVisible(visible: Boolean) {
        _visible.value = visible
    }
}

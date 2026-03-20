package com.codeprep.app.feedback

enum class SoundCue {
    TapPrimary,
    Success,
    Error
}

enum class HapticCue {
    TapPrimary,
    Success,
    Error
}

sealed interface FeedbackEvent {
    data object TapPrimary : FeedbackEvent
    data object Success : FeedbackEvent
    data object Error : FeedbackEvent
}

interface FeedbackSettings {
    fun isSoundEffectsEnabled(): Boolean

    fun isHapticsEnabled(): Boolean
}

interface SoundOutput {
    fun play(cue: SoundCue)
}

interface HapticOutput {
    fun perform(cue: HapticCue)
}

interface AppFeedback {
    fun emit(event: FeedbackEvent)
}

class AppFeedbackManager(
    private val settings: FeedbackSettings,
    private val soundOutput: SoundOutput,
    private val hapticOutput: HapticOutput,
    private val clock: () -> Long
) : AppFeedback {
    private var lastTapPrimaryAtMs: Long? = null

    override fun emit(event: FeedbackEvent) {
        when (event) {
            FeedbackEvent.TapPrimary -> emitTapPrimary()
            FeedbackEvent.Success -> emitSuccess()
            FeedbackEvent.Error -> emitError()
        }
    }

    private fun emitTapPrimary() {
        val nowMs = clock()
        val shouldSuppress = lastTapPrimaryAtMs?.let { nowMs - it < TAP_SUPPRESSION_WINDOW_MS } == true
        if (shouldSuppress) return

        lastTapPrimaryAtMs = nowMs
        if (settings.isSoundEffectsEnabled()) {
            soundOutput.play(SoundCue.TapPrimary)
        }
        if (settings.isHapticsEnabled()) {
            hapticOutput.perform(HapticCue.TapPrimary)
        }
    }

    private fun emitSuccess() {
        if (settings.isSoundEffectsEnabled()) {
            soundOutput.play(SoundCue.Success)
        }
        if (settings.isHapticsEnabled()) {
            hapticOutput.perform(HapticCue.Success)
        }
    }

    private fun emitError() {
        if (settings.isSoundEffectsEnabled()) {
            soundOutput.play(SoundCue.Error)
        }
        if (settings.isHapticsEnabled()) {
            hapticOutput.perform(HapticCue.Error)
        }
    }

    private companion object {
        const val TAP_SUPPRESSION_WINDOW_MS = 150L
    }
}

package com.codeprep.app.feedback

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFeedbackManagerTest {

    @Test
    fun emit_skipsTapWhenSameActionChainAlreadyPlayedRecently() {
        val soundOutput = FakeSoundOutput()
        val hapticOutput = FakeHapticOutput()
        val settings = FakeFeedbackSettings(soundEnabled = true, hapticsEnabled = true)
        val clock = FakeClock(1_000L)
        val manager = AppFeedbackManager(
            settings = settings,
            soundOutput = soundOutput,
            hapticOutput = hapticOutput,
            clock = clock::now
        )

        manager.emit(FeedbackEvent.TapPrimary)
        manager.emit(FeedbackEvent.TapPrimary)

        assertEquals(listOf(SoundCue.TapPrimary), soundOutput.played)
        assertEquals(listOf(HapticCue.TapPrimary), hapticOutput.performed)
    }

    @Test
    fun emit_respectsDisabledSoundSettingWhileKeepingHapticsEnabled() {
        val soundOutput = FakeSoundOutput()
        val hapticOutput = FakeHapticOutput()
        val settings = FakeFeedbackSettings(soundEnabled = false, hapticsEnabled = true)
        val clock = FakeClock(5_000L)
        val manager = AppFeedbackManager(
            settings = settings,
            soundOutput = soundOutput,
            hapticOutput = hapticOutput,
            clock = clock::now
        )

        manager.emit(FeedbackEvent.Success)

        assertEquals(emptyList<SoundCue>(), soundOutput.played)
        assertEquals(listOf(HapticCue.Success), hapticOutput.performed)
    }

    @Test
    fun emit_playsErrorFeedbackWhenEnabled() {
        val soundOutput = FakeSoundOutput()
        val hapticOutput = FakeHapticOutput()
        val settings = FakeFeedbackSettings(soundEnabled = true, hapticsEnabled = true)
        val clock = FakeClock(9_000L)
        val manager = AppFeedbackManager(
            settings = settings,
            soundOutput = soundOutput,
            hapticOutput = hapticOutput,
            clock = clock::now
        )

        manager.emit(FeedbackEvent.Error)

        assertEquals(listOf(SoundCue.Error), soundOutput.played)
        assertEquals(listOf(HapticCue.Error), hapticOutput.performed)
    }
}

private class FakeFeedbackSettings(
    private val soundEnabled: Boolean,
    private val hapticsEnabled: Boolean
) : FeedbackSettings {
    override fun isSoundEffectsEnabled(): Boolean = soundEnabled

    override fun isHapticsEnabled(): Boolean = hapticsEnabled
}

private class FakeSoundOutput : SoundOutput {
    val played = mutableListOf<SoundCue>()

    override fun play(cue: SoundCue) {
        played += cue
    }
}

private class FakeHapticOutput : HapticOutput {
    val performed = mutableListOf<HapticCue>()

    override fun perform(cue: HapticCue) {
        performed += cue
    }
}

private class FakeClock(
    private var currentTimeMs: Long
) {
    fun now(): Long = currentTimeMs
}

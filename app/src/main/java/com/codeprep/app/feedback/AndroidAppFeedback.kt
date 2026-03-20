package com.codeprep.app.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.codeprep.app.data.settings.AppSettingsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsFeedbackSettings @Inject constructor(
    private val appSettingsStore: AppSettingsStore
) : FeedbackSettings {
    override fun isSoundEffectsEnabled(): Boolean = appSettingsStore.isSoundEffectsEnabled()

    override fun isHapticsEnabled(): Boolean = appSettingsStore.isHapticsEnabled()
}

@Singleton
class ToneGeneratorSoundOutput @Inject constructor() : SoundOutput {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, SOUND_VOLUME)

    override fun play(cue: SoundCue) {
        val (tone, durationMs) = when (cue) {
            SoundCue.TapPrimary -> ToneGenerator.TONE_PROP_ACK to 35
            SoundCue.Success -> ToneGenerator.TONE_PROP_BEEP2 to 80
            SoundCue.Error -> ToneGenerator.TONE_PROP_NACK to 70
        }

        synchronized(toneGenerator) {
            runCatching {
                toneGenerator.startTone(tone, durationMs)
            }
        }
    }

    private companion object {
        const val SOUND_VOLUME = 35
    }
}

@Singleton
class AndroidHapticOutput @Inject constructor(
    @param:ApplicationContext private val context: Context
) : HapticOutput {
    override fun perform(cue: HapticCue) {
        val vibrator = resolveVibrator() ?: return
        if (!vibrator.hasVibrator()) return

        val effect = when (cue) {
            HapticCue.TapPrimary -> VibrationEffect.createOneShot(12L, 70)
            HapticCue.Success -> VibrationEffect.createWaveform(
                longArrayOf(0L, 16L, 28L, 22L),
                intArrayOf(0, 90, 0, 140),
                -1
            )
            HapticCue.Error -> VibrationEffect.createOneShot(24L, 160)
        }

        runCatching {
            vibrator.vibrate(effect)
        }
    }

    private fun resolveVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FeedbackModule {

    @Provides
    @Singleton
    fun provideFeedbackSettings(
        appSettingsStore: AppSettingsStore
    ): FeedbackSettings = AppSettingsFeedbackSettings(appSettingsStore)

    @Provides
    @Singleton
    fun provideSoundOutput(): SoundOutput = ToneGeneratorSoundOutput()

    @Provides
    @Singleton
    fun provideHapticOutput(
        @ApplicationContext context: Context
    ): HapticOutput = AndroidHapticOutput(context)

    @Provides
    @Singleton
    fun provideAppFeedback(
        settings: FeedbackSettings,
        soundOutput: SoundOutput,
        hapticOutput: HapticOutput
    ): AppFeedback = AppFeedbackManager(
        settings = settings,
        soundOutput = soundOutput,
        hapticOutput = hapticOutput,
        clock = SystemClock::elapsedRealtime
    )
}

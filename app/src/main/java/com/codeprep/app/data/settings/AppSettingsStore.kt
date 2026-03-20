package com.codeprep.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@Singleton
class AppSettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun selectedLanguage(): Flow<String> = callbackFlow {
        trySend(getSelectedLanguage())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SELECTED_LANGUAGE) {
                trySend(getSelectedLanguage())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.conflate()

    fun soundEffectsEnabled(): Flow<Boolean> = callbackFlow {
        trySend(isSoundEffectsEnabled())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SOUND_EFFECTS_ENABLED) {
                trySend(isSoundEffectsEnabled())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.conflate()

    fun hapticsEnabled(): Flow<Boolean> = callbackFlow {
        trySend(isHapticsEnabled())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_HAPTICS_ENABLED) {
                trySend(isHapticsEnabled())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.conflate()

    fun getSelectedLanguage(): String {
        return preferences.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE).orEmpty()
            .ifBlank { DEFAULT_LANGUAGE }
    }

    fun setSelectedLanguage(languageCode: String) {
        preferences.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
    }

    fun isSoundEffectsEnabled(): Boolean {
        return preferences.getBoolean(KEY_SOUND_EFFECTS_ENABLED, true)
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_SOUND_EFFECTS_ENABLED, enabled).apply()
    }

    fun isHapticsEnabled(): Boolean {
        return preferences.getBoolean(KEY_HAPTICS_ENABLED, true)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_SOUND_EFFECTS_ENABLED = "sound_effects_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val DEFAULT_LANGUAGE = "en"
    }
}

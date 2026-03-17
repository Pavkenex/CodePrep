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

    fun getSelectedLanguage(): String {
        return preferences.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE).orEmpty()
            .ifBlank { DEFAULT_LANGUAGE }
    }

    fun setSelectedLanguage(languageCode: String) {
        preferences.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        const val DEFAULT_LANGUAGE = "en"
    }
}

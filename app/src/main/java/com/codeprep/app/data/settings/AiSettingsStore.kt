package com.codeprep.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Stores the user-configured AI provider settings (API key, model ID, base URL)
 * in [EncryptedSharedPreferences]. Mirrors the [AppSettingsStore] pattern.
 *
 * A blank `apiKey` means "unconfigured". Any encryption failure (e.g. Keystore
 * issues on some devices) degrades to "unconfigured" instead of crashing.
 */
@Singleton
class AiSettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences? = runCatching {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            masterKeyAlias,
            PREFS_NAME,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrNull()

    fun apiKey(): Flow<String> = stringFlow(KEY_API_KEY, DEFAULT_API_KEY)
    fun modelId(): Flow<String> = stringFlow(KEY_MODEL_ID, DEFAULT_MODEL_ID)
    fun baseUrl(): Flow<String> = stringFlow(KEY_BASE_URL, DEFAULT_BASE_URL)

    fun getApiKey(): String = read(KEY_API_KEY, DEFAULT_API_KEY)
    fun getModelId(): String = read(KEY_MODEL_ID, DEFAULT_MODEL_ID)
    fun getBaseUrl(): String = read(KEY_BASE_URL, DEFAULT_BASE_URL)

    fun setApiKey(value: String) = write(KEY_API_KEY, value)
    fun setModelId(value: String) = write(KEY_MODEL_ID, value)
    fun setBaseUrl(value: String) = write(KEY_BASE_URL, value)

    private fun stringFlow(key: String, default: String): Flow<String> = callbackFlow {
        trySend(read(key, default))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(read(key, default))
            }
        }
        preferences?.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences?.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.conflate()

    private fun read(key: String, default: String): String {
        return runCatching {
            preferences?.getString(key, default).orEmpty()
                .ifBlank { default }
        }.getOrDefault(default)
    }

    private fun write(key: String, value: String) {
        runCatching {
            preferences?.edit()?.putString(key, value)?.apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "ai_settings"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL_ID = "model_id"
        private const val KEY_BASE_URL = "base_url"

        const val DEFAULT_API_KEY = ""
        const val DEFAULT_MODEL_ID = "stepfun/step-3.5-flash:freegi"
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1/"
    }
}

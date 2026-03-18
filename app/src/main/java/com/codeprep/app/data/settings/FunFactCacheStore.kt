package com.codeprep.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import com.codeprep.app.data.model.DailyFunFact
import com.codeprep.app.data.model.FunFactCacheSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FunFactCacheStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(fact: DailyFunFact) {
        preferences.edit()
            .putString(KEY_ID, fact.id)
            .putString(KEY_TEXT, fact.text)
            .putString(KEY_LANGUAGE, fact.languageCode)
            .putLong(KEY_EPOCH_DAY, fact.epochDay)
            .apply()
    }

    fun get(): FunFactCacheSnapshot? {
        val id = preferences.getString(KEY_ID, null).orEmpty()
        val text = preferences.getString(KEY_TEXT, null).orEmpty()
        if (id.isBlank() || text.isBlank()) return null

        return FunFactCacheSnapshot(
            id = id,
            text = text,
            languageCode = preferences.getString(KEY_LANGUAGE, AppSettingsStore.DEFAULT_LANGUAGE)
                .orEmpty()
                .ifBlank { AppSettingsStore.DEFAULT_LANGUAGE },
            epochDay = preferences.getLong(KEY_EPOCH_DAY, Long.MIN_VALUE)
        )
    }

    companion object {
        private const val PREFS_NAME = "fun_fact_cache"
        private const val KEY_ID = "cached_fun_fact_id"
        private const val KEY_TEXT = "cached_fun_fact_text"
        private const val KEY_LANGUAGE = "cached_fun_fact_language"
        private const val KEY_EPOCH_DAY = "cached_fun_fact_epoch_day"
    }
}

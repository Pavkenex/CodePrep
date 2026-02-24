package com.codeprep.app.data.remote.api

import com.codeprep.app.BuildConfig

object AiConfig {
    const val MODEL = "deepseek/deepseek-r1-0528:free"
    const val BASE_URL = "https://openrouter.ai/api/v1/"
    const val MAX_TOKENS = 500
    const val CACHE_VALIDITY_DAYS = 30L
    const val MAX_QUESTIONS_PER_DAY = 20
    const val COOLDOWN_BETWEEN_QUESTIONS_MS = 10_000L

    // ⚠️ U produkciji: koristiti Firebase Cloud Function proxy!
    val apiKey: String get() = BuildConfig.OPENROUTER_API_KEY
}
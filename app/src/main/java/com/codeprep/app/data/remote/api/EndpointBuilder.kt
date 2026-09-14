package com.codeprep.app.data.remote.api

/**
 * Joins a user-configured base URL with a Retrofit-style endpoint path.
 *
 * The base URL is user-provided (Settings → AI), so it may or may not end
 * with `/v1`, may carry trailing slashes, or may be blank. A blank base URL
 * falls back to the default OpenRouter endpoint; the fallback lives here
 * rather than in the settings store because the store must stay blank when
 * the user has not configured a provider.
 */
object EndpointBuilder {
    const val CHAT_COMPLETIONS_PATH = "chat/completions"

    private const val FALLBACK_BASE_URL = "https://openrouter.ai/api/v1/"

    fun build(
        baseUrl: String?,
        path: String = CHAT_COMPLETIONS_PATH
    ): String {
        val base = baseUrl?.trim().orEmpty().ifBlank { FALLBACK_BASE_URL }
        return base.trimEnd('/') + "/" + path.trimStart('/')
    }
}

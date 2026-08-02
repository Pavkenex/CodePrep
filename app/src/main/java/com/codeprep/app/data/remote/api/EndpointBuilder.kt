package com.codeprep.app.data.remote.api

import com.codeprep.app.data.settings.AiSettingsStore

/**
 * Joins a user-configured base URL with a Retrofit-style endpoint path.
 *
 * The base URL is user-provided (Settings → AI), so it may or may not end
 * with `/v1`, may carry trailing slashes, or may be blank. A blank base URL
 * falls back to the default OpenRouter endpoint.
 */
object EndpointBuilder {
    const val CHAT_COMPLETIONS_PATH = "chat/completions"

    fun build(
        baseUrl: String?,
        path: String = CHAT_COMPLETIONS_PATH
    ): String {
        val base = baseUrl?.trim().orEmpty().ifBlank { AiSettingsStore.DEFAULT_BASE_URL }
        return base.trimEnd('/') + "/" + path.trimStart('/')
    }
}

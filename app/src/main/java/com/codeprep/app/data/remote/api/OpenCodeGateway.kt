package com.codeprep.app.data.remote.api

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.UUID

/**
 * Transport requirements for the OpenCode Go gateway (provider id `opencode-go`).
 *
 * The gateway rejects requests that do not carry a stable session identifier,
 * so every call aimed at an opencode.ai endpoint gets an `x-opencode-session`
 * header plus a CodePrep-specific user agent. Other providers (OpenRouter and
 * any custom OpenAI-compatible endpoint) receive neither header, so their
 * requests are byte-for-byte unchanged.
 */
object OpenCodeGateway {
    const val HOST = "opencode.ai"
    const val USER_AGENT = "CodePrep/1.0"

    private const val SESSION_PREFIX = "codeprep-"

    fun isOpenCodeEndpoint(url: String?): Boolean {
        val host = url?.trim()?.toHttpUrlOrNull()?.host ?: return false
        return host == HOST || host.endsWith(".$HOST")
    }

    /**
     * Returns the `x-opencode-session` value for [conversationKey] (a stable id
     * such as a lesson id), or null when the endpoint is not OpenCode Go.
     * A blank key falls back to a one-off random id for single requests such as
     * the connection test.
     */
    fun sessionIdFor(url: String?, conversationKey: String?): String? {
        if (!isOpenCodeEndpoint(url)) return null
        val key = conversationKey?.trim().orEmpty().ifBlank { UUID.randomUUID().toString() }
        return SESSION_PREFIX + key
    }

    fun userAgentFor(url: String?): String? {
        return if (isOpenCodeEndpoint(url)) USER_AGENT else null
    }
}

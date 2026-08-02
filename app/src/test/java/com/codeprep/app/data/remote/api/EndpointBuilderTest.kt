package com.codeprep.app.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test

class EndpointBuilderTest {

    @Test
    fun `joins plain base url with chat completions path`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("https://openrouter.ai/api/v1/")
        )
    }

    @Test
    fun `strips trailing slashes before joining the path`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("https://openrouter.ai/api/v1///")
        )
    }

    @Test
    fun `keeps a base url without v1 prefix intact`() {
        assertEquals(
            "https://openrouter.ai/api/chat/completions",
            EndpointBuilder.build("https://openrouter.ai/api")
        )
    }

    @Test
    fun `keeps a base url ending in v1 without extra slash intact`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("https://openrouter.ai/api/v1")
        )
    }

    @Test
    fun `falls back to the default base url when the configured one is blank`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("")
        )
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("   ")
        )
    }

    @Test
    fun `falls back to the default base url when the configured one is null`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build(null)
        )
    }

    @Test
    fun `trims surrounding whitespace from the configured base url`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            EndpointBuilder.build("  https://openrouter.ai/api/v1/  ")
        )
    }

    @Test
    fun `supports a custom endpoint path`() {
        assertEquals(
            "https://openrouter.ai/api/v1/models",
            EndpointBuilder.build("https://openrouter.ai/api/v1/", "models")
        )
    }
}

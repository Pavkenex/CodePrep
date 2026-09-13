package com.codeprep.app.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenCodeGatewayTest {

    @Test
    fun `opencode endpoint is recognized by host`() {
        assertTrue(
            OpenCodeGateway.isOpenCodeEndpoint(
                "https://opencode.ai/zen/go/v1/chat/completions"
            )
        )
        assertTrue(
            OpenCodeGateway.isOpenCodeEndpoint(
                "https://opencode.ai:443/zen/go/v1/chat/completions"
            )
        )
        assertFalse(
            OpenCodeGateway.isOpenCodeEndpoint(
                "https://openrouter.ai/api/v1/chat/completions"
            )
        )
    }

    @Test
    fun `malformed or blank urls are not treated as opencode endpoints`() {
        assertFalse(OpenCodeGateway.isOpenCodeEndpoint("not a url"))
        assertFalse(OpenCodeGateway.isOpenCodeEndpoint(""))
        assertFalse(OpenCodeGateway.isOpenCodeEndpoint(null))
    }

    @Test
    fun `session header is only produced for opencode endpoints`() {
        assertNull(
            OpenCodeGateway.sessionIdFor(
                url = "https://openrouter.ai/api/v1/chat/completions",
                conversationKey = "lesson-1"
            )
        )
        assertNull(OpenCodeGateway.sessionIdFor(url = null, conversationKey = "lesson-1"))
    }

    @Test
    fun `session id is stable for the same conversation`() {
        val url = "https://opencode.ai/zen/go/v1/chat/completions"

        val first = OpenCodeGateway.sessionIdFor(url, conversationKey = "lesson-42")
        val second = OpenCodeGateway.sessionIdFor(url, conversationKey = "lesson-42")

        assertEquals(first, second)
        assertTrue(first!!.isNotBlank())
    }

    @Test
    fun `different conversations get different session ids`() {
        val url = "https://opencode.ai/zen/go/v1/chat/completions"

        assertNotEquals(
            OpenCodeGateway.sessionIdFor(url, conversationKey = "lesson-1"),
            OpenCodeGateway.sessionIdFor(url, conversationKey = "lesson-2")
        )
    }

    @Test
    fun `missing conversation key still produces a session id`() {
        val url = "https://opencode.ai/zen/go/v1/chat/completions"

        val first = OpenCodeGateway.sessionIdFor(url, conversationKey = null)
        val second = OpenCodeGateway.sessionIdFor(url, conversationKey = " ")

        assertTrue(first!!.isNotBlank())
        assertTrue(second!!.isNotBlank())
    }

    @Test
    fun `user agent is only produced for opencode endpoints`() {
        assertEquals(
            "CodePrep/1.0",
            OpenCodeGateway.userAgentFor("https://opencode.ai/zen/go/v1/chat/completions")
        )
        assertNull(OpenCodeGateway.userAgentFor("https://openrouter.ai/api/v1/chat/completions"))
        assertNull(OpenCodeGateway.userAgentFor("not a url"))
    }
}

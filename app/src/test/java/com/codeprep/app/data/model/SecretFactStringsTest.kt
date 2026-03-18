package com.codeprep.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretFactStringsTest {

    @Test
    fun `returns serbian hint for sr language`() {
        assertEquals("Tajna zanimljivost je spremna...", SecretFactStrings.armedHint("sr"))
    }

    @Test
    fun `falls back to english for unsupported language`() {
        assertTrue(SecretFactStrings.revealTitle("de").contains("Secret"))
    }

    @Test
    fun `returns serbian dismiss action for sr language`() {
        assertEquals("Zatvori", SecretFactStrings.dismissAction("sr"))
    }

    @Test
    fun `returns english fallback fact message for unsupported language`() {
        assertEquals("No secret fact available right now.", SecretFactStrings.fallbackFact("de"))
    }

    @Test
    fun `returns serbian category label for algorithm category`() {
        assertEquals("Algoritmi", SecretFactStrings.categoryLabel("algorithms", "sr"))
    }

    @Test
    fun `falls back to generic english category for unknown category key`() {
        assertEquals("Programming", SecretFactStrings.categoryLabel("unknown-key", "en"))
    }
}

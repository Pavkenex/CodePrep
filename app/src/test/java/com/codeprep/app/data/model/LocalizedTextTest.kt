package com.codeprep.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedTextTest {

    @Test
    fun `resolve returns Serbian text when selected language is Serbian`() {
        val text = LocalizedText(
            en = "Debugging started with a moth.",
            sr = "Debagovanje je pocelo sa moljcem."
        )

        assertEquals("Debagovanje je pocelo sa moljcem.", text.resolve("sr"))
    }

    @Test
    fun `resolve falls back to English when Serbian text is missing`() {
        val text = LocalizedText(
            en = "The first computer bug was a real moth.",
            sr = ""
        )

        assertEquals("The first computer bug was a real moth.", text.resolve("sr"))
    }
}

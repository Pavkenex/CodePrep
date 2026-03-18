package com.codeprep.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FunFactRotationTest {

    @Test
    fun `sortFacts orders by numeric suffix before lexical fallback`() {
        val sorted = FunFactRotation.sortFacts(
            listOf(
                FunFact("ff_010", LocalizedText(en = "Ten")),
                FunFact("ff_002", LocalizedText(en = "Two")),
                FunFact("ff_100", LocalizedText(en = "Hundred"))
            )
        )

        assertEquals(listOf("ff_002", "ff_010", "ff_100"), sorted.map(FunFact::id))
    }

    @Test
    fun `indexForDate uses epoch day progression rather than day of month`() {
        val firstIndex = FunFactRotation.indexForDate(LocalDate.of(2026, 3, 31), factCount = 100)
        val secondIndex = FunFactRotation.indexForDate(LocalDate.of(2026, 4, 1), factCount = 100)

        assertEquals((firstIndex + 1) % 100, secondIndex)
    }

    @Test
    fun `randomIndex stays inside bounds`() {
        repeat(20) {
            val index = FunFactRotation.randomIndex(
                date = LocalDate.of(2026, 3, 18),
                factCount = 5,
                salt = it.toLong()
            )

            assertTrue(index in 0..4)
        }
    }
}

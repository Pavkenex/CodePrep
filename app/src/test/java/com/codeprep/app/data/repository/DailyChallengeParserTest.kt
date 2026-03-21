package com.codeprep.app.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DailyChallengeParserTest {

    @Test
    fun `parses localized daily challenge structure with stable option ids`() {
        val data = mapOf(
            "id" to "daily-2026-03-20",
            "title" to mapOf(
                "en" to "Daily Challenge",
                "sr" to "Dnevni izazov"
            ),
            "prompt" to mapOf(
                "en" to "What does this code print?",
                "sr" to "Sta ispisuje ovaj kod?"
            ),
            "explanation" to mapOf(
                "en" to "Because the variable is incremented once.",
                "sr" to "Zato sto se promenljiva uveca jednom."
            ),
            "options" to listOf(
                mapOf(
                    "id" to "a",
                    "text" to mapOf("en" to "0", "sr" to "0")
                ),
                mapOf(
                    "id" to "b",
                    "text" to mapOf("en" to "1", "sr" to "1")
                ),
                mapOf(
                    "id" to "c",
                    "text" to mapOf("en" to "2", "sr" to "2")
                )
            ),
            "correctOptionId" to "b",
            "challengeType" to "output_prediction",
            "difficulty" to "medium",
            "pointsReward" to 15L,
            "codeSnippet" to mapOf(
                "language" to "kotlin",
                "code" to "val count = 0\nprintln(count + 1)"
            )
        )

        val question = parseDailyChallengeQuestion(
            data = data,
            selectedLanguage = "sr",
            fallbackId = "fallback-id"
        )

        assertNotNull(question)
        assertEquals("daily-2026-03-20", question?.id)
        assertEquals("Dnevni izazov", question?.title)
        assertEquals("Sta ispisuje ovaj kod?", question?.text)
        assertEquals(listOf("0", "1", "2"), question?.options)
        assertEquals(1, question?.correctIndex)
        assertEquals("Zato sto se promenljiva uveca jednom.", question?.explanation)
        assertEquals("output_prediction", question?.type)
        assertEquals("medium", question?.difficulty)
        assertEquals(15.0, question?.difficultyWeight)
        assertEquals("kotlin", question?.codeSnippetLanguage)
        assertEquals("val count = 0\nprintln(count + 1)", question?.codeSnippet)
    }
}

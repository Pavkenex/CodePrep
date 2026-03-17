package com.codeprep.app.data.repository

import com.codeprep.app.data.local.entity.LessonProgressEntity
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonProgressRulesTest {

    @Test
    fun `50 percent score passes lesson and grants base xp once`() {
        val result = LessonProgressRules.evaluateAttempt(
            existing = null,
            userId = "u1",
            lessonId = "l1",
            correctCount = 2,
            totalQuestions = 4,
            mistakeCount = 2,
            baseXp = 20,
            attemptedAt = Instant.EPOCH
        )

        assertTrue(result.passed)
        assertFalse(result.perfect)
        assertTrue(result.awardedBaseXp)
        assertFalse(result.awardedPerfectBonus)
        assertEquals(20, result.xpAwarded)
        assertTrue(result.progress.completed)
        assertFalse(result.progress.perfectRun)
        assertTrue(result.progress.completionXpAwarded)
        assertFalse(result.progress.perfectBonusAwarded)
    }

    @Test
    fun `perfect first attempt grants base xp and perfect bonus`() {
        val result = LessonProgressRules.evaluateAttempt(
            existing = null,
            userId = "u1",
            lessonId = "l1",
            correctCount = 5,
            totalQuestions = 5,
            mistakeCount = 0,
            baseXp = 30,
            attemptedAt = Instant.EPOCH
        )

        assertTrue(result.passed)
        assertTrue(result.perfect)
        assertTrue(result.awardedBaseXp)
        assertTrue(result.awardedPerfectBonus)
        assertEquals(45, result.xpAwarded)
        assertTrue(result.progress.completed)
        assertTrue(result.progress.perfectRun)
    }

    @Test
    fun `perfect retry after prior pass grants only bonus xp`() {
        val existing = LessonProgressEntity(
            userId = "u1",
            lessonId = "l1",
            completed = true,
            perfectRun = false,
            bestCorrectCount = 3,
            totalQuestions = 5,
            mistakeCount = 2,
            completionXpAwarded = true,
            perfectBonusAwarded = false,
            lastAttemptAt = Instant.EPOCH
        )

        val result = LessonProgressRules.evaluateAttempt(
            existing = existing,
            userId = "u1",
            lessonId = "l1",
            correctCount = 5,
            totalQuestions = 5,
            mistakeCount = 0,
            baseXp = 30,
            attemptedAt = Instant.EPOCH.plusSeconds(60)
        )

        assertTrue(result.passed)
        assertTrue(result.perfect)
        assertFalse(result.awardedBaseXp)
        assertTrue(result.awardedPerfectBonus)
        assertEquals(15, result.xpAwarded)
        assertTrue(result.progress.perfectRun)
        assertTrue(result.progress.perfectBonusAwarded)
    }

    @Test
    fun `repeating perfect attempt after rewards grants no xp`() {
        val existing = LessonProgressEntity(
            userId = "u1",
            lessonId = "l1",
            completed = true,
            perfectRun = true,
            bestCorrectCount = 5,
            totalQuestions = 5,
            mistakeCount = 0,
            completionXpAwarded = true,
            perfectBonusAwarded = true,
            lastAttemptAt = Instant.EPOCH
        )

        val result = LessonProgressRules.evaluateAttempt(
            existing = existing,
            userId = "u1",
            lessonId = "l1",
            correctCount = 5,
            totalQuestions = 5,
            mistakeCount = 0,
            baseXp = 30,
            attemptedAt = Instant.EPOCH.plusSeconds(60)
        )

        assertTrue(result.passed)
        assertTrue(result.perfect)
        assertFalse(result.awardedBaseXp)
        assertFalse(result.awardedPerfectBonus)
        assertEquals(0, result.xpAwarded)
    }
}

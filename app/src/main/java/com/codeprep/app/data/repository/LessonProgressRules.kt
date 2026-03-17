package com.codeprep.app.data.repository

import com.codeprep.app.data.local.entity.LessonProgressEntity
import java.time.Instant
import kotlin.math.max

object LessonProgressRules {
    fun evaluateAttempt(
        existing: LessonProgressEntity?,
        userId: String,
        lessonId: String,
        correctCount: Int,
        totalQuestions: Int,
        mistakeCount: Int,
        baseXp: Int,
        attemptedAt: Instant = Instant.now()
    ): EvaluatedAttempt {
        val passed = totalQuestions > 0 && correctCount * 2 >= totalQuestions
        val perfect = totalQuestions > 0 && correctCount == totalQuestions
        val awardedBaseXp = passed && existing?.completionXpAwarded != true
        val awardedPerfectBonus = perfect && existing?.perfectBonusAwarded != true
        val perfectBonusXp = calculatePerfectBonusXp(baseXp)

        val attempt = LessonProgressEntity(
            userId = userId,
            lessonId = lessonId,
            completed = passed,
            perfectRun = perfect,
            bestCorrectCount = correctCount,
            totalQuestions = totalQuestions,
            mistakeCount = mistakeCount,
            completionXpAwarded = awardedBaseXp,
            perfectBonusAwarded = awardedPerfectBonus,
            lastAttemptAt = attemptedAt
        )
        val mergedAttempt = existing?.mergeWith(attempt) ?: attempt

        val xpAwarded =
            (if (awardedBaseXp) baseXp else 0) + (if (awardedPerfectBonus) perfectBonusXp else 0)

        return EvaluatedAttempt(
            progress = mergedAttempt,
            passed = passed,
            perfect = perfect,
            xpAwarded = xpAwarded,
            awardedBaseXp = awardedBaseXp,
            awardedPerfectBonus = awardedPerfectBonus,
            perfectBonusXp = perfectBonusXp
        )
    }

    fun calculatePerfectBonusXp(baseXp: Int): Int {
        return max((baseXp * 0.5f).toInt(), 5)
    }

    fun LessonProgressEntity.mergeWith(other: LessonProgressEntity): LessonProgressEntity {
        val mergedAttemptAt = listOfNotNull(lastAttemptAt, other.lastAttemptAt).maxOrNull()
        val mergedMistakeCount = listOf(mistakeCount, other.mistakeCount)
            .filter { it >= 0 }
            .minOrNull()
            ?: 0

        return LessonProgressEntity(
            userId = userId,
            lessonId = lessonId,
            completed = completed || other.completed,
            perfectRun = perfectRun || other.perfectRun,
            bestCorrectCount = max(bestCorrectCount, other.bestCorrectCount),
            totalQuestions = max(totalQuestions, other.totalQuestions),
            mistakeCount = mergedMistakeCount,
            completionXpAwarded = completionXpAwarded || other.completionXpAwarded,
            perfectBonusAwarded = perfectBonusAwarded || other.perfectBonusAwarded,
            lastAttemptAt = mergedAttemptAt
        )
    }

    data class EvaluatedAttempt(
        val progress: LessonProgressEntity,
        val passed: Boolean,
        val perfect: Boolean,
        val xpAwarded: Int,
        val awardedBaseXp: Boolean,
        val awardedPerfectBonus: Boolean,
        val perfectBonusXp: Int
    )
}

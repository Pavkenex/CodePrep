package com.codeprep.app.work

import com.codeprep.app.data.local.entity.UserProgressEntity
import com.codeprep.app.data.repository.UserRepository
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

object HeartReminderSchedule {

    fun delayFor(
        progress: UserProgressEntity,
        now: Instant = Instant.now()
    ): Duration? {
        if (progress.hearts >= UserRepository.MAX_HEARTS || progress.lastHeartLostAt == null) {
            return null
        }

        val missingHearts = (UserRepository.MAX_HEARTS - progress.hearts).coerceAtLeast(0)
        val fullRefillAt = progress.lastHeartLostAt.plus(
            (missingHearts * UserRepository.HEART_REFILL_MINUTES).toLong(),
            ChronoUnit.MINUTES
        )
        val remaining = Duration.between(now, fullRefillAt)
        return if (remaining.isNegative) Duration.ZERO else remaining
    }
}

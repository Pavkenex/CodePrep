package com.codeprep.app.work

import com.codeprep.app.data.local.entity.UserProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class HeartReminderScheduleTest {

    @Test
    fun delayFor_returnsRemainingTimeUntilAllHeartsAreRestored() {
        val progress = progress(
            hearts = 3,
            lastHeartLostAt = Instant.parse("2026-03-19T10:00:00Z")
        )

        val delay = HeartReminderSchedule.delayFor(
            progress = progress,
            now = Instant.parse("2026-03-19T10:10:00Z")
        )

        assertEquals(Duration.ofMinutes(50), delay)
    }

    @Test
    fun delayFor_returnsZeroWhenReminderIsAlreadyDue() {
        val progress = progress(
            hearts = 4,
            lastHeartLostAt = Instant.parse("2026-03-19T10:00:00Z")
        )

        val delay = HeartReminderSchedule.delayFor(
            progress = progress,
            now = Instant.parse("2026-03-19T10:45:00Z")
        )

        assertEquals(Duration.ZERO, delay)
    }

    @Test
    fun delayFor_returnsNullWhenHeartsAreAlreadyFull() {
        val progress = progress(
            hearts = 5,
            lastHeartLostAt = Instant.parse("2026-03-19T10:00:00Z")
        )

        val delay = HeartReminderSchedule.delayFor(
            progress = progress,
            now = Instant.parse("2026-03-19T10:10:00Z")
        )

        assertNull(delay)
    }

    private fun progress(hearts: Int, lastHeartLostAt: Instant?): UserProgressEntity {
        return UserProgressEntity(
            userId = "user-1",
            nickname = "tester",
            xp = 0,
            level = 1,
            streak = 0,
            hearts = hearts,
            lastHeartLostAt = lastHeartLostAt,
            lastActiveDate = null,
            updatedAt = Instant.parse("2026-03-19T10:00:00Z")
        )
    }
}

package com.codeprep.app.ui.navigation

import com.codeprep.app.data.local.entity.UserProgressEntity
import com.codeprep.app.data.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.Instant

class HeartRefillCountdownTest {

    @Test
    fun calculateTimeUntilNextHeart_returnsNullWhenHeartsAreFull() {
        val progress = progress(hearts = 5, lastHeartLostAt = Instant.parse("2026-03-16T10:00:00Z"))

        val remaining = UserRepository.calculateTimeUntilNextHeartAt(
            progress = progress,
            now = Instant.parse("2026-03-16T10:10:00Z")
        )

        assertNull(remaining)
    }

    @Test
    fun calculateTimeUntilNextHeart_returnsRemainingDuration() {
        val progress = progress(hearts = 3, lastHeartLostAt = Instant.parse("2026-03-16T10:00:00Z"))

        val remaining = UserRepository.calculateTimeUntilNextHeartAt(
            progress = progress,
            now = Instant.parse("2026-03-16T10:25:00Z")
        )

        assertEquals(Duration.ofMinutes(5), remaining)
    }

    @Test
    fun formatHeartRefillCountdown_formatsMinutesAndSeconds() {
        val label = formatHeartRefillCountdown(Duration.ofMinutes(12).plusSeconds(43))

        assertEquals("12:43", label)
    }

    @Test
    fun formatHeartRefillCountdown_formatsHoursWhenNeeded() {
        val label = formatHeartRefillCountdown(Duration.ofHours(1).plusMinutes(2).plusSeconds(3))

        assertEquals("1:02:03", label)
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
            updatedAt = Instant.parse("2026-03-16T10:00:00Z")
        )
    }
}

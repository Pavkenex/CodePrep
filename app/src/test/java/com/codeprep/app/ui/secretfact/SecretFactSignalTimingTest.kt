package com.codeprep.app.ui.secretfact

import org.junit.Assert.assertEquals
import org.junit.Test

class SecretFactSignalTimingTest {

    @Test
    fun `returns remaining delay when signal has not lasted long enough`() {
        assertEquals(
            350L,
            remainingSignalDelayMillis(
                signalStartedAt = 1_000L,
                now = 1_550L,
                minimumDurationMillis = 900L
            )
        )
    }

    @Test
    fun `returns zero when signal already lasted long enough`() {
        assertEquals(
            0L,
            remainingSignalDelayMillis(
                signalStartedAt = 1_000L,
                now = 2_200L,
                minimumDurationMillis = 900L
            )
        )
    }
}

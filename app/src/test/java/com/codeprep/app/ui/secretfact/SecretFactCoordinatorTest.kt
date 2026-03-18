package com.codeprep.app.ui.secretfact

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecretFactCoordinatorTest {

    @Test
    fun `arms only on supported route in portrait`() {
        val state = SecretFactCoordinator().reduce(
            state = SecretFactState(),
            event = SecretFactEvent.ShakeDetected(
                route = "home",
                isLandscape = false
            )
        )

        assertEquals(SecretFactPhase.Armed, state.phase)
        assertTrue(state.isHintVisible)
    }

    @Test
    fun `ignores shake on blocked route`() {
        val state = SecretFactCoordinator().reduce(
            state = SecretFactState(),
            event = SecretFactEvent.ShakeDetected(
                route = "quiz/lesson-1",
                isLandscape = false
            )
        )

        assertEquals(SecretFactPhase.Idle, state.phase)
        assertFalse(state.isHintVisible)
    }

    @Test
    fun `reveals only after arm then fresh landscape rotation`() {
        val coordinator = SecretFactCoordinator()
        val armed = coordinator.reduce(
            SecretFactState(),
            SecretFactEvent.ShakeDetected(route = "home", isLandscape = false)
        )

        val revealed = coordinator.reduce(
            armed,
            SecretFactEvent.OrientationChanged(isLandscape = true)
        )

        assertEquals(SecretFactPhase.Revealed, revealed.phase)
    }
}

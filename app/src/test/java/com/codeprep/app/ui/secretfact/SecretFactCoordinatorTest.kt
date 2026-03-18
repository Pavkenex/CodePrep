package com.codeprep.app.ui.secretfact

import org.junit.Assert.assertEquals
import org.junit.Test

class SecretFactCoordinatorTest {

    @Test
    fun `shake enters signaling on supported route`() {
        val state = SecretFactCoordinator().reduce(
            state = SecretFactState(),
            event = SecretFactEvent.ShakeDetected(
                route = "home",
                isLandscape = false
            )
        )

        assertEquals(SecretFactPhase.Signaling, state.phase)
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
    }

    @Test
    fun `fact resolution reveals only after signal phase`() {
        val coordinator = SecretFactCoordinator()
        val signaling = coordinator.reduce(
            state = SecretFactState(),
            event = SecretFactEvent.ShakeDetected(
                route = "profile",
                isLandscape = true
            )
        )

        val revealed = coordinator.reduce(
            state = signaling,
            event = SecretFactEvent.FactResolved(hasFact = true)
        )

        assertEquals(SecretFactPhase.Revealed, revealed.phase)
    }

    @Test
    fun `empty fact resolution returns to idle instead of showing fallback card`() {
        val coordinator = SecretFactCoordinator()
        val signaling = coordinator.reduce(
            SecretFactState(),
            SecretFactEvent.ShakeDetected(route = "home", isLandscape = false)
        )

        val reset = coordinator.reduce(
            signaling,
            SecretFactEvent.FactResolved(hasFact = false)
        )

        assertEquals(SecretFactPhase.Idle, reset.phase)
    }

    @Test
    fun `route change to blocked screen resets revealed state`() {
        val coordinator = SecretFactCoordinator()
        val signaling = coordinator.reduce(
            SecretFactState(),
            SecretFactEvent.ShakeDetected(route = "home", isLandscape = false)
        )
        val revealed = coordinator.reduce(signaling, SecretFactEvent.FactResolved(hasFact = true))

        val reset = coordinator.reduce(
            revealed,
            SecretFactEvent.RouteChanged(route = "quiz/lesson-1")
        )

        assertEquals(SecretFactPhase.Idle, reset.phase)
    }
}

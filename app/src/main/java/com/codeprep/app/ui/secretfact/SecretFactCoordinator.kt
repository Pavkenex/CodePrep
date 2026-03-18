package com.codeprep.app.ui.secretfact

class SecretFactCoordinator {

    fun reduce(
        state: SecretFactState,
        event: SecretFactEvent
    ): SecretFactState {
        return when (event) {
            is SecretFactEvent.ShakeDetected -> handleShake(event)
            is SecretFactEvent.FactResolved -> handleFactResolved(state, event)
            is SecretFactEvent.OrientationChanged -> state
            is SecretFactEvent.RouteChanged -> handleRouteChange(state, event)
            SecretFactEvent.TimeoutExpired,
            SecretFactEvent.Dismissed -> SecretFactState()
        }
    }

    private fun handleShake(event: SecretFactEvent.ShakeDetected): SecretFactState {
        if (!SecretFactRoutes.isSupported(event.route)) {
            return SecretFactState()
        }

        return SecretFactState(
            phase = SecretFactPhase.Signaling,
            currentRoute = event.route
        )
    }

    private fun handleFactResolved(
        state: SecretFactState,
        event: SecretFactEvent.FactResolved
    ): SecretFactState {
        if (state.phase != SecretFactPhase.Signaling) {
            return state
        }

        return if (event.hasFact) {
            state.copy(phase = SecretFactPhase.Revealed)
        } else {
            SecretFactState()
        }
    }

    private fun handleRouteChange(
        state: SecretFactState,
        event: SecretFactEvent.RouteChanged
    ): SecretFactState {
        val route = event.route ?: return SecretFactState()
        if (!SecretFactRoutes.isSupported(route)) {
            return SecretFactState()
        }

        return state.copy(currentRoute = route)
    }
}

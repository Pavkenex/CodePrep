package com.codeprep.app.ui.secretfact

class SecretFactCoordinator {

    fun reduce(
        state: SecretFactState,
        event: SecretFactEvent
    ): SecretFactState {
        return when (event) {
            is SecretFactEvent.ShakeDetected -> handleShake(event)
            is SecretFactEvent.OrientationChanged -> handleOrientation(state, event)
            SecretFactEvent.TimeoutExpired,
            SecretFactEvent.Dismissed -> SecretFactState()
        }
    }

    private fun handleShake(event: SecretFactEvent.ShakeDetected): SecretFactState {
        if (event.isLandscape || !SecretFactRoutes.isSupported(event.route)) {
            return SecretFactState()
        }

        return SecretFactState(
            phase = SecretFactPhase.Armed,
            isHintVisible = true,
            currentRoute = event.route,
            requiresFreshLandscapeRotation = true
        )
    }

    private fun handleOrientation(
        state: SecretFactState,
        event: SecretFactEvent.OrientationChanged
    ): SecretFactState {
        if (
            state.phase != SecretFactPhase.Armed ||
            !state.requiresFreshLandscapeRotation ||
            !event.isLandscape
        ) {
            return state
        }

        return state.copy(
            phase = SecretFactPhase.Revealed,
            isHintVisible = false,
            requiresFreshLandscapeRotation = false
        )
    }
}

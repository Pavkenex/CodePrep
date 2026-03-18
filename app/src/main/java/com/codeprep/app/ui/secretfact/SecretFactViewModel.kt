package com.codeprep.app.ui.secretfact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.model.SecretFactStrings
import com.codeprep.app.data.repository.FunFactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SecretFactViewModel @Inject constructor(
    private val funFactRepository: FunFactRepository
) : ViewModel() {
    private val coordinator = SecretFactCoordinator()
    private val _uiState = MutableStateFlow(SecretFactUiState())
    val uiState: StateFlow<SecretFactUiState> = _uiState.asStateFlow()

    private var activeFactRequestSalt: Long? = null

    fun onShake(route: String, isLandscape: Boolean) {
        val reducedState = coordinator.reduce(
            state = _uiState.value.toDomainState(),
            event = SecretFactEvent.ShakeDetected(route, isLandscape)
        )
        val revealNonce = System.currentTimeMillis()
        applyReducedState(reducedState, revealNonce = revealNonce)
        if (reducedState.phase == SecretFactPhase.Signaling) {
            preloadFact(revealNonce)
        }
    }

    fun onOrientationChanged(isLandscape: Boolean) = Unit

    fun onRouteChanged(route: String?) {
        val reducedState = coordinator.reduce(
            state = _uiState.value.toDomainState(),
            event = SecretFactEvent.RouteChanged(route)
        )
        applyReducedState(reducedState)
    }

    fun onDismiss() {
        activeFactRequestSalt = null
        applyReducedState(
            coordinator.reduce(
                state = _uiState.value.toDomainState(),
                event = SecretFactEvent.Dismissed
            )
        )
    }

    private fun preloadFact(revealNonce: Long) {
        activeFactRequestSalt = revealNonce

        viewModelScope.launch {
            val language = funFactRepository.getSelectedLanguage()
            val fact = funFactRepository.getRandomFact(salt = revealNonce)
            if (activeFactRequestSalt != revealNonce) return@launch

            val remainingSignalDelay = remainingSignalDelayMillis(
                signalStartedAt = revealNonce,
                now = System.currentTimeMillis(),
                minimumDurationMillis = MINIMUM_SIGNAL_DURATION_MILLIS
            )
            if (remainingSignalDelay > 0L) {
                delay(remainingSignalDelay)
            }
            if (activeFactRequestSalt != revealNonce) return@launch

            val reducedState = coordinator.reduce(
                state = _uiState.value.toDomainState(),
                event = SecretFactEvent.FactResolved(hasFact = !fact?.text.isNullOrBlank())
            )

            if (reducedState.phase != SecretFactPhase.Revealed || fact == null || fact.text.isBlank()) {
                activeFactRequestSalt = null
                applyReducedState(reducedState)
                return@launch
            }

            activeFactRequestSalt = null
            _uiState.update { state ->
                state.copy(
                    phase = reducedState.phase,
                    currentRoute = reducedState.currentRoute,
                    title = SecretFactStrings.revealTitle(language),
                    factText = fact.text,
                    categoryLabel = SecretFactStrings.categoryLabel(fact.categoryKey, language),
                    languageCode = language
                )
            }
        }
    }

    private fun applyReducedState(
        state: SecretFactState,
        revealNonce: Long? = null
    ) {
        val language = funFactRepository.getSelectedLanguage()
        _uiState.update { current ->
            current.copy(
                phase = state.phase,
                currentRoute = state.currentRoute,
                title = if (state.phase == SecretFactPhase.Revealed) current.title else "",
                factText = if (state.phase == SecretFactPhase.Revealed) current.factText else "",
                categoryLabel = if (state.phase == SecretFactPhase.Revealed) current.categoryLabel else "",
                languageCode = language,
                revealNonce = revealNonce ?: current.revealNonce
            )
        }
    }

    private fun SecretFactUiState.toDomainState(): SecretFactState {
        return SecretFactState(
            phase = phase,
            currentRoute = currentRoute
        )
    }

    private companion object {
        const val MINIMUM_SIGNAL_DURATION_MILLIS = 950L
    }
}

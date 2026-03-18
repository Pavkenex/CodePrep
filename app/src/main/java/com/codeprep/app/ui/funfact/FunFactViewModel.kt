package com.codeprep.app.ui.funfact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.model.FunFactStrings
import com.codeprep.app.data.repository.FunFactRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FunFactUiState(
    val isLoading: Boolean = true,
    val requiresLoginRedirect: Boolean = false,
    val title: String = "",
    val factText: String = "",
    val tapHint: String = "",
    val buttonText: String = ""
)

@HiltViewModel
class FunFactViewModel @Inject constructor(
    private val funFactRepository: FunFactRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(FunFactUiState())
    val uiState: StateFlow<FunFactUiState> = _uiState.asStateFlow()

    init {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val language = funFactRepository.getSelectedLanguage()
            _uiState.value = FunFactUiState(
                isLoading = false,
                requiresLoginRedirect = true,
                title = FunFactStrings.splashTitle(language),
                factText = FunFactStrings.neutralMessage(language),
                tapHint = FunFactStrings.tapHint(language),
                buttonText = FunFactStrings.openButton(language)
            )
        } else {
            loadFact()
        }
    }

    private fun loadFact() {
        viewModelScope.launch {
            val language = funFactRepository.getSelectedLanguage()
            val fact = funFactRepository.getTodayFact()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = FunFactStrings.splashTitle(language),
                    factText = fact?.text?.takeIf(String::isNotBlank)
                        ?: FunFactStrings.fallbackMessage(language),
                    tapHint = FunFactStrings.tapHint(language),
                    buttonText = FunFactStrings.openButton(language)
                )
            }
        }
    }
}

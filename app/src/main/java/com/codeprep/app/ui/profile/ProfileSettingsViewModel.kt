package com.codeprep.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.settings.AppSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val appSettingsStore: AppSettingsStore
) : ViewModel() {
    val selectedLanguage = appSettingsStore.selectedLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettingsStore.DEFAULT_LANGUAGE
        )

    fun setSelectedLanguage(languageCode: String) {
        appSettingsStore.setSelectedLanguage(languageCode)
    }
}

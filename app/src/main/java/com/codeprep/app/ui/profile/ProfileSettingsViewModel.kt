package com.codeprep.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.settings.AppSettingsStore
import com.codeprep.app.widget.FunFactWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val appSettingsStore: AppSettingsStore,
    private val funFactWidgetUpdater: FunFactWidgetUpdater
) : ViewModel() {
    val selectedLanguage = appSettingsStore.selectedLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettingsStore.DEFAULT_LANGUAGE
        )

    fun setSelectedLanguage(languageCode: String) {
        appSettingsStore.setSelectedLanguage(languageCode)
        viewModelScope.launch {
            funFactWidgetUpdater.updateAllWidgets()
        }
    }
}

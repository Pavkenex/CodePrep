package com.codeprep.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.repository.AiRepository
import com.codeprep.app.data.settings.AppSettingsStore
import com.codeprep.app.widget.FunFactWidgetUpdater
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val appSettingsStore: AppSettingsStore,
    private val funFactWidgetUpdater: FunFactWidgetUpdater,
    private val aiRepository: AiRepository,
    auth: FirebaseAuth
) : ViewModel() {
    private val userId: String = auth.currentUser?.uid ?: ""

    val selectedLanguage = appSettingsStore.selectedLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettingsStore.DEFAULT_LANGUAGE
        )
    val soundEffectsEnabled = appSettingsStore.soundEffectsEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )
    val hapticsEnabled = appSettingsStore.hapticsEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    fun setSelectedLanguage(languageCode: String) {
        appSettingsStore.setSelectedLanguage(languageCode)
        viewModelScope.launch {
            funFactWidgetUpdater.updateAllWidgets()
        }
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        appSettingsStore.setSoundEffectsEnabled(enabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        appSettingsStore.setHapticsEnabled(enabled)
    }

    suspend fun createConversationExport(): ConversationExportPayload {
        return ConversationExportPayload(
            fileName = "codeprep-conversations-backup.json",
            content = aiRepository.exportSavedConversations(userId)
        )
    }

    suspend fun importConversationBackup(payload: String): ConversationImportResult {
        return runCatching {
            aiRepository.importSavedConversations(userId, payload)
        }.fold(
            onSuccess = { ConversationImportResult.Success(importedCount = it) },
            onFailure = {
                if (it is IllegalArgumentException) {
                    ConversationImportResult.InvalidFile
                } else {
                    ConversationImportResult.Error
                }
            }
        )
    }
}

data class ConversationExportPayload(
    val fileName: String,
    val content: String
)

sealed interface ConversationImportResult {
    data class Success(val importedCount: Int) : ConversationImportResult
    data object InvalidFile : ConversationImportResult
    data object Error : ConversationImportResult
}

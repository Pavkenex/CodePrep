package com.codeprep.app.ui.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.R
import com.codeprep.app.data.remote.api.AiMessage
import com.codeprep.app.data.remote.api.AiRequest
import com.codeprep.app.data.remote.api.OpenRouterApi
import com.codeprep.app.data.settings.AiSettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val aiSettingsStore: AiSettingsStore,
    private val api: OpenRouterApi
) : ViewModel() {

    // Seed each state with the synchronous store getter so the very first
    // collection already carries the persisted values; otherwise the draft
    // fields in AiSettingsSection capture "" and a subsequent Save/Test would
    // wipe the stored key.
    val apiKey = aiSettingsStore.apiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), aiSettingsStore.getApiKey())

    val modelId = aiSettingsStore.modelId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), aiSettingsStore.getModelId())

    val baseUrl = aiSettingsStore.baseUrl()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), aiSettingsStore.getBaseUrl())

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection = _isTestingConnection.asStateFlow()

    private val _connectionTestResult = MutableStateFlow<AiConnectionTestResult?>(null)
    val connectionTestResult = _connectionTestResult.asStateFlow()

    fun save(apiKey: String, modelId: String, baseUrl: String) {
        aiSettingsStore.setApiKey(apiKey.trim())
        aiSettingsStore.setModelId(modelId.trim().ifBlank { AiSettingsStore.DEFAULT_MODEL_ID })
        aiSettingsStore.setBaseUrl(baseUrl.trim().ifBlank { AiSettingsStore.DEFAULT_BASE_URL })
    }

    suspend fun testConnection(apiKey: String, modelId: String, baseUrl: String): AiConnectionTestResult {
        _isTestingConnection.value = true
        _connectionTestResult.value = null
        // Persist the typed values first so the ping travels through the same
        // interceptor path as production calls, validating exactly what the
        // next real question will use.
        save(apiKey, modelId, baseUrl)
        val resolvedModelId = modelId.trim().ifBlank { AiSettingsStore.DEFAULT_MODEL_ID }
        val result = runCatching {
            api.askQuestion(
                AiRequest(
                    model = resolvedModelId,
                    messages = listOf(AiMessage(role = "user", content = "ping")),
                    max_tokens = 1
                )
            )
        }.fold(
            onSuccess = { AiConnectionTestResult.Success },
            onFailure = { throwable ->
                when {
                    throwable is HttpException && throwable.code() == 401 -> AiConnectionTestResult.InvalidKey
                    // 404 and any other HTTP status mean the provider answered
                    // with the wrong endpoint or model; a malformed base URL
                    // surfaces as IllegalArgumentException here.
                    throwable is HttpException -> AiConnectionTestResult.BadEndpoint
                    throwable is IllegalArgumentException -> AiConnectionTestResult.BadEndpoint
                    else -> AiConnectionTestResult.Unreachable
                }
            }
        )
        _connectionTestResult.value = result
        _isTestingConnection.value = false
        return result
    }
}

sealed interface AiConnectionTestResult {
    @get:StringRes
    val messageResId: Int
    val isError: Boolean

    data object Success : AiConnectionTestResult {
        override val messageResId = R.string.profile_ai_test_success
        override val isError = false
    }

    data object InvalidKey : AiConnectionTestResult {
        override val messageResId = R.string.profile_ai_test_invalid_key
        override val isError = true
    }

    data object BadEndpoint : AiConnectionTestResult {
        override val messageResId = R.string.profile_ai_test_bad_endpoint
        override val isError = true
    }

    data object Unreachable : AiConnectionTestResult {
        override val messageResId = R.string.profile_ai_test_unreachable
        override val isError = true
    }
}

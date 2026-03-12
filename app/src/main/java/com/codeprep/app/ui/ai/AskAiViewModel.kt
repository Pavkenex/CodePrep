package com.codeprep.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.AiExplanationEntity
import com.codeprep.app.data.remote.api.AiConfig
import com.codeprep.app.data.repository.AiRepository
import com.codeprep.app.domain.model.AiResponse
import com.codeprep.app.domain.model.LessonContext
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AskAiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    auth: FirebaseAuth
) : ViewModel() {
    private val userId: String = auth.currentUser?.uid ?: ""

    private val _answer = MutableStateFlow<AiResponse?>(null)
    val answer: StateFlow<AiResponse?> = _answer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val history: StateFlow<List<AiExplanationEntity>> = aiRepository.getHistory(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var lastQuestionTime: Long = 0L

    fun ask(question: String, context: LessonContext?) {
        val normalizedQuestion = question.trim()
        if (normalizedQuestion.isBlank()) {
            _answer.value = AiResponse.Error("Unesi pitanje pre slanja.")
            return
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastQuestionTime
        if (elapsed < AiConfig.COOLDOWN_BETWEEN_QUESTIONS_MS) {
            val remaining = AiConfig.COOLDOWN_BETWEEN_QUESTIONS_MS - elapsed
            val waitSeconds = (remaining + 999L) / 1000L
            _answer.value = AiResponse.Error("Sačekaj još ${waitSeconds}s pre sledećeg pitanja.")
            return
        }

        lastQuestionTime = now
        viewModelScope.launch {
            _isLoading.value = true
            _answer.value = aiRepository.askQuestion(userId, normalizedQuestion, context)
            _isLoading.value = false
        }
    }

    fun clearAnswer() {
        _answer.value = null
    }
}

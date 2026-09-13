package com.codeprep.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.R
import com.codeprep.app.data.remote.api.AiConfig
import com.codeprep.app.data.repository.AiRepository
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.settings.AiSettingsStore
import com.codeprep.app.data.settings.AppSettingsStore
import com.codeprep.app.data.settings.AppStringProvider
import com.codeprep.app.domain.model.AiConversationMessage
import com.codeprep.app.domain.model.AiConversationRole
import com.codeprep.app.domain.model.AiResponse
import com.codeprep.app.domain.model.LessonContext
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AskAiViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    courseRepository: CourseRepository,
    appSettingsStore: AppSettingsStore,
    aiSettingsStore: AiSettingsStore,
    private val strings: AppStringProvider,
    auth: FirebaseAuth,
    private val overlayVisibility: AskAiOverlayVisibility
) : ViewModel() {
    private val userId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(AskAiUiState())
    val uiState: StateFlow<AskAiUiState> = _uiState.asStateFlow()

    /** True when the user has configured an API key; gates the whole Ask AI flow. */
    val hasApiKey = aiSettingsStore.apiKey()
        .map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val savedConversations = aiRepository.getSavedConversations(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedLanguage = appSettingsStore.selectedLanguage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettingsStore.DEFAULT_LANGUAGE)

    val explanationsUiState = combine(
        courseRepository.getCourses(),
        courseRepository.getAllLessons(),
        savedConversations,
        selectedLanguage
    ) { courses, lessons, savedConversations, languageCode ->
        val modules = buildExplanationsModules(
            courses = courses,
            lessons = lessons,
            savedConversations = savedConversations,
            languageCode = languageCode
        )

        ExplanationsUiState(
            modules = modules,
            isLoading = savedConversations.isNotEmpty() && modules.isEmpty(),
            isEmpty = savedConversations.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExplanationsUiState())

    private var currentLessonContext: LessonContext? = null
    private var currentLessonId: String? = null
    private var savedConversationId: String? = null
    private var lastSavedMessageCount = 0

    private var lastQuestionTime: Long = 0L

    init {
        viewModelScope.launch {
            courseRepository.refreshCourses()
        }
    }

    fun onOverlayVisibilityChanged(visible: Boolean) {
        overlayVisibility.setVisible(visible)
    }

    fun bindLesson(context: LessonContext) {
        if (currentLessonId == context.lessonId && _uiState.value.hasLoadedLesson) {
            return
        }

        currentLessonContext = context
        currentLessonId = context.lessonId
        viewModelScope.launch {
            val savedConversation = aiRepository.getSavedConversation(userId, context.lessonId)
            val loadedMessages = savedConversation?.messages.orEmpty()
            savedConversationId = savedConversation?.id
            lastSavedMessageCount = loadedMessages.size
            _uiState.value = AskAiUiState(
                courseTitle = context.courseTitle,
                lessonTitle = context.lessonTitle,
                messages = loadedMessages,
                isLoading = false,
                hasLoadedLesson = true,
                isSaved = savedConversation != null,
                hasDraft = loadedMessages.isNotEmpty(),
                canSave = loadedMessages.isNotEmpty() && userId.isNotBlank()
            )
        }
    }

    fun ask(question: String) {
        val normalizedQuestion = question.trim()
        if (normalizedQuestion.isBlank()) {
            appendSystemMessage(strings.get(R.string.ask_ai_error_empty_input))
            return
        }

        // No network call is made in the locked state; the UI renders a locked
        // card instead, this is a safety net for any other entry point.
        if (!hasApiKey.value) {
            appendSystemMessage(strings.get(R.string.ai_key_required_system_message))
            return
        }

        val now = System.currentTimeMillis()
        val elapsed = now - lastQuestionTime
        if (elapsed < AiConfig.COOLDOWN_BETWEEN_QUESTIONS_MS) {
            val remaining = AiConfig.COOLDOWN_BETWEEN_QUESTIONS_MS - elapsed
            val waitSeconds = (remaining + 999L) / 1000L
            appendSystemMessage(strings.get(R.string.ask_ai_error_wait_seconds, waitSeconds))
            return
        }

        val context = currentLessonContext
        if (context == null) {
            appendSystemMessage(strings.get(R.string.ask_ai_error_not_ready))
            return
        }

        lastQuestionTime = now
        val userMessage = conversationMessage(AiConversationRole.User, normalizedQuestion, now)
        val historyBeforeRequest = _uiState.value.messages.filterConversationHistory()
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            hasDraft = true,
            isSaved = false,
            canSave = userId.isNotBlank()
        )

        viewModelScope.launch {
            val response = aiRepository.askQuestion(
                userId = userId,
                question = normalizedQuestion,
                context = context,
                conversationHistory = historyBeforeRequest
            )
            applyResponse(response)
        }
    }

    fun saveConversation() {
        val context = currentLessonContext ?: return
        val messagesToSave = _uiState.value.messages.filterConversationHistory()
        if (messagesToSave.isEmpty() || userId.isBlank()) {
            return
        }

        viewModelScope.launch {
            val savedConversation = aiRepository.saveConversation(
                userId = userId,
                context = context,
                messages = messagesToSave
            )
            savedConversationId = savedConversation.id
            lastSavedMessageCount = savedConversation.messages.size
            _uiState.value = _uiState.value.copy(
                courseTitle = savedConversation.courseTitle,
                lessonTitle = savedConversation.lessonTitle,
                isSaved = true,
                hasDraft = savedConversation.messages.isNotEmpty(),
                canSave = savedConversation.messages.isNotEmpty() && userId.isNotBlank()
            )
        }
    }

    fun deleteSavedConversation(lessonId: String) {
        if (lessonId.isBlank() || userId.isBlank()) {
            return
        }

        viewModelScope.launch {
            aiRepository.deleteSavedConversation(userId, lessonId)
        }
    }

    private fun applyResponse(response: AiResponse) {
        val message = when (response) {
            is AiResponse.Success -> conversationMessage(
                role = AiConversationRole.Assistant,
                content = response.answer
            )

            is AiResponse.Fallback -> conversationMessage(
                role = AiConversationRole.Assistant,
                content = response.summary
            )

            is AiResponse.Error -> conversationMessage(
                role = AiConversationRole.System,
                content = response.message
            )

            AiResponse.RateLimited -> conversationMessage(
                role = AiConversationRole.System,
                content = strings.get(R.string.ai_rate_limited, AiConfig.MAX_QUESTIONS_PER_DAY)
            )
        }

        val updatedMessages = _uiState.value.messages + message
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isLoading = false,
            hasDraft = updatedMessages.isNotEmpty(),
            isSaved = savedConversationId != null && updatedMessages.countPersistableMessages() == lastSavedMessageCount,
            canSave = updatedMessages.countPersistableMessages() > 0 && userId.isNotBlank()
        )
    }

    private fun appendSystemMessage(content: String) {
        val updatedMessages = _uiState.value.messages + conversationMessage(
            role = AiConversationRole.System,
            content = content
        )
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            hasDraft = updatedMessages.isNotEmpty(),
            isSaved = false,
            canSave = updatedMessages.countPersistableMessages() > 0 && userId.isNotBlank()
        )
    }
}

private fun List<AiConversationMessage>.filterConversationHistory(): List<AiConversationMessage> {
    return filter { it.role != AiConversationRole.System }
}

private fun List<AiConversationMessage>.countPersistableMessages(): Int {
    return count { it.role != AiConversationRole.System }
}

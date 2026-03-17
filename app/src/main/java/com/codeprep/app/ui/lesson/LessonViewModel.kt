package com.codeprep.app.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.model.LocalizedText
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.data.settings.AppSettingsStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    appSettingsStore: AppSettingsStore,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val lessonId: String = savedStateHandle["lessonId"] ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""
    private val fallbackNickname: String? =
        auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }

    private val _lesson = MutableStateFlow<CachedLessonEntity?>(null)
    val lesson: StateFlow<CachedLessonEntity?> = _lesson.asStateFlow()

    private val _courseTitle = MutableStateFlow<String?>(null)
    val courseTitle: StateFlow<String?> = _courseTitle.asStateFlow()

    val selectedLanguage = appSettingsStore.selectedLanguage()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), AppSettingsStore.DEFAULT_LANGUAGE)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startQuizEvent = MutableSharedFlow<String>()
    val startQuizEvent: SharedFlow<String> = _startQuizEvent.asSharedFlow()

    private val _quizBlockMessage = MutableStateFlow<String?>(null)
    val quizBlockMessage: StateFlow<String?> = _quizBlockMessage.asStateFlow()

    val lessonProgress = if (userId.isBlank()) {
        flowOf<LessonProgressEntity?>(null)
    } else {
        lessonProgressRepository.observeProgressForUser(userId).map { progressList ->
            progressList.firstOrNull { it.lessonId == lessonId }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            val loadedLesson = courseRepository.getLesson(lessonId)
            _lesson.value = loadedLesson
            _courseTitle.value = loadedLesson?.let { courseRepository.getCourseTitle(it.courseId) }
            _isLoading.value = false

            if (userId.isNotBlank()) {
                userRepository.registerStreakActivity(userId)
            }
        }
    }

    fun buildAiSummary(lesson: CachedLessonEntity?, language: String): String {
        if (lesson == null) return ""

        return buildString {
            appendSection("Introduction", lesson.content.introduction, language)
            appendSection("Explanation", lesson.content.explanation, language)
            appendSection("Example", lesson.content.example, language)
            appendSection("Key takeaway", lesson.content.keyTakeaway, language)

            val analogy = lesson.analogy?.resolve(language).orEmpty()
            if (analogy.isNotBlank()) {
                appendLine("Analogy: $analogy")
            }

            if (lesson.keyPoints.isNotEmpty()) {
                appendLine("Key points:")
                lesson.keyPoints
                    .map { it.resolve(language) }
                    .filter { it.isNotBlank() }
                    .forEach { appendLine("- $it") }
            }
        }.trim()
    }

    fun clearQuizBlockMessage() {
        _quizBlockMessage.value = null
    }

    fun onStartQuizClicked() {
        if (lessonId.isBlank()) return

        _quizBlockMessage.value = null

        viewModelScope.launch {
            if (userId.isBlank()) {
                _quizBlockMessage.value = "Moraš biti ulogovan da bi započeo kviz."
                return@launch
            }

            // Retry hydration a few times — login may still be writing to Room
            var hydrated = false
            repeat(5) {
                hydrated = userRepository.ensureLocalUserProgress(userId, fallbackNickname)
                if (hydrated) return@repeat
                kotlinx.coroutines.delay(400)
            }
            if (!hydrated) {
                _quizBlockMessage.value = "Profil nije učitan. Probaj ponovo za trenutak."
                return@launch
            }

            userRepository.refillHearts(userId)
            val userProgress = userRepository.getUserProgressOnce(userId)
            val lessonProgress = lessonProgressRepository.getLessonProgress(userId, lessonId)

            if (userProgress == null) {
                _quizBlockMessage.value = "Profil nije učitan. Probaj ponovo za trenutak."
                return@launch
            }

            val hasHearts = userProgress.hearts > 0
            val canReplayCompletedLesson = lessonProgress?.completed == true

            if (hasHearts || canReplayCompletedLesson) {
                _startQuizEvent.emit(lessonId)
            } else {
                _quizBlockMessage.value = "Nemaš srca za novu lekciju trenutno. (❤️ ${userProgress.hearts})"
            }
        }
    }

    private fun StringBuilder.appendSection(
        label: String,
        content: LocalizedText,
        language: String
    ) {
        val resolved = content.resolve(language)
        if (resolved.isBlank()) return
        appendLine("$label: $resolved")
    }
}

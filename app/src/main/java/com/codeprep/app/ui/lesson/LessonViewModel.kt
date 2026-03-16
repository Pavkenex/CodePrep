package com.codeprep.app.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val lessonProgressRepository: LessonProgressRepository,
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _startQuizEvent = MutableSharedFlow<String>()
    val startQuizEvent: SharedFlow<String> = _startQuizEvent.asSharedFlow()

    private val _quizBlockMessage = MutableStateFlow<String?>(null)
    val quizBlockMessage: StateFlow<String?> = _quizBlockMessage.asStateFlow()

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
}

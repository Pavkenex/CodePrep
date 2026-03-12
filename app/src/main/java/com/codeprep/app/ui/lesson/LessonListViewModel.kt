package com.codeprep.app.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LessonListItemUi(
    val lesson: CachedLessonEntity,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val isPerfect: Boolean,
    val isBlockedByHearts: Boolean,
    val canOpen: Boolean
)

data class LessonListUiState(
    val hearts: Int = 5,
    val lessons: List<LessonListItemUi> = emptyList()
)

@HiltViewModel
class LessonListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    private val userRepository: UserRepository,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: String = savedStateHandle["courseId"] ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""

    val uiState: StateFlow<LessonListUiState> = combine(
        courseRepository.getLessonsForCourse(courseId),
        if (userId.isBlank()) flowOf(emptyList()) else lessonProgressRepository.observeProgressForUser(userId),
        if (userId.isBlank()) flowOf(null) else userRepository.getUserProgress(userId)
    ) { lessons, progressList, userProgress ->
        val hearts = userProgress?.hearts ?: 5
        val lessonItems = buildLessonItems(lessons, progressList, hearts)
        LessonListUiState(hearts = hearts, lessons = lessonItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LessonListUiState())

    init {
        refreshHearts()
    }

    fun refreshHearts() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            userRepository.refillHearts(userId)
        }
    }

    private fun buildLessonItems(
        lessons: List<CachedLessonEntity>,
        progressList: List<LessonProgressEntity>,
        hearts: Int
    ): List<LessonListItemUi> {
        val progressByLesson = progressList.associateBy { it.lessonId }
        val sortedLessons = lessons.sortedBy { it.orderIndex }

        return sortedLessons.mapIndexed { index, lesson ->
            val progress = progressByLesson[lesson.lessonId]
            val isCompleted = progress?.completed == true
            val isPerfect = progress?.perfectRun == true

            val isUnlocked = if (index == 0) {
                true
            } else {
                val previousLesson = sortedLessons[index - 1]
                progressByLesson[previousLesson.lessonId]?.perfectRun == true
            }

            val isBlockedByHearts = isUnlocked && hearts <= 0 && !isCompleted
            LessonListItemUi(
                lesson = lesson,
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                isPerfect = isPerfect,
                isBlockedByHearts = isBlockedByHearts,
                canOpen = isUnlocked && !isBlockedByHearts
            )
        }
    }
}

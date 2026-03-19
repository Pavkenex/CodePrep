package com.codeprep.app.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.local.entity.LessonProgressEntity
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.repository.UserRepository
import com.codeprep.app.data.settings.AppSettingsStore
import com.codeprep.app.work.WorkScheduler
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
    val title: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val isPerfect: Boolean,
    val isActive: Boolean,
    val isBlockedByHearts: Boolean,
    val canOpen: Boolean,
    val progressLabel: String
)

data class LessonListUiState(
    val moduleTitle: String = "",
    val moduleDescription: String = "",
    val moduleLocked: Boolean = false,
    val hearts: Int = 5,
    val lessons: List<LessonListItemUi> = emptyList()
)

@HiltViewModel
class LessonListViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val lessonProgressRepository: LessonProgressRepository,
    private val userRepository: UserRepository,
    private val workScheduler: WorkScheduler,
    appSettingsStore: AppSettingsStore,
    auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val courseId: String = savedStateHandle["courseId"] ?: ""
    private val userId: String = auth.currentUser?.uid ?: ""

    val uiState: StateFlow<LessonListUiState> = combine(
        courseRepository.getCourses(),
        courseRepository.getLessonsForCourse(courseId),
        if (userId.isBlank()) flowOf(emptyList()) else lessonProgressRepository.observeProgressForUser(userId),
        if (userId.isBlank()) flowOf(null) else userRepository.getUserProgress(userId),
        appSettingsStore.selectedLanguage()
    ) { modules, lessons, progressList, userProgress, language ->
        val module = modules.firstOrNull { it.courseId == courseId }
        val hearts = userProgress?.hearts ?: 5
        val moduleLocked = module?.isLocked == true
        val lessonItems = buildLessonItems(lessons, progressList, hearts, language, moduleLocked)
        LessonListUiState(
            moduleTitle = module?.title?.resolve(language).orEmpty(),
            moduleDescription = module?.description?.resolve(language).orEmpty(),
            moduleLocked = moduleLocked,
            hearts = hearts,
            lessons = lessonItems
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LessonListUiState())

    init {
        refreshHearts()
    }

    fun refreshHearts() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            userRepository.refillHearts(userId)
            workScheduler.syncHeartReminder(userId)
        }
    }

    private fun buildLessonItems(
        lessons: List<CachedLessonEntity>,
        progressList: List<LessonProgressEntity>,
        hearts: Int,
        language: String,
        moduleLocked: Boolean
    ): List<LessonListItemUi> {
        val progressByLesson = progressList.associateBy { it.lessonId }
        val sortedLessons = lessons.sortedBy { it.orderIndex }
        val activeLessonId = sortedLessons.firstOrNull { lesson ->
            !moduleLocked &&
                isLessonUnlocked(sortedLessons, lesson, progressByLesson) &&
                progressByLesson[lesson.lessonId]?.completed != true
        }?.lessonId

        return sortedLessons.mapIndexed { index, lesson ->
            val progress = progressByLesson[lesson.lessonId]
            val isCompleted = progress?.completed == true
            val isPerfect = progress?.perfectRun == true

            val isUnlocked = if (moduleLocked) {
                false
            } else if (index == 0) {
                true
            } else {
                val previousLesson = sortedLessons[index - 1]
                progressByLesson[previousLesson.lessonId]?.completed == true
            }

            val isBlockedByHearts = isUnlocked && hearts <= 0 && !isCompleted
            LessonListItemUi(
                lesson = lesson,
                title = lesson.title.resolve(language),
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                isPerfect = isPerfect,
                isActive = lesson.lessonId == activeLessonId,
                isBlockedByHearts = isBlockedByHearts,
                canOpen = isUnlocked && !isBlockedByHearts,
                progressLabel = when {
                    progress == null || progress.totalQuestions == 0 -> "New lesson"
                    isPerfect -> "Perfect ${progress.bestCorrectCount}/${progress.totalQuestions}"
                    isCompleted -> "Passed ${progress.bestCorrectCount}/${progress.totalQuestions}"
                    else -> "Best ${progress.bestCorrectCount}/${progress.totalQuestions}"
                }
            )
        }
    }

    private fun isLessonUnlocked(
        sortedLessons: List<CachedLessonEntity>,
        lesson: CachedLessonEntity,
        progressByLesson: Map<String, LessonProgressEntity>
    ): Boolean {
        val index = sortedLessons.indexOfFirst { it.lessonId == lesson.lessonId }
        if (index <= 0) return true
        val previousLessonId = sortedLessons[index - 1].lessonId
        return progressByLesson[previousLessonId]?.completed == true
    }
}

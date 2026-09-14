package com.codeprep.app.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.repository.CourseRepository
import com.codeprep.app.data.repository.LessonProgressRepository
import com.codeprep.app.data.settings.AppSettingsStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModuleCardUi(
    val courseId: String,
    val title: String,
    val description: String,
    val icon: String,
    val isLocked: Boolean,
    val lessonCount: Int,
    val completedLessons: Int,
    val perfectLessons: Int,
    val continueLessonId: String?,
    val continueLessonTitle: String?
)

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    lessonProgressRepository: LessonProgressRepository,
    appSettingsStore: AppSettingsStore,
    auth: FirebaseAuth
) : ViewModel() {
    private val userId = auth.currentUser?.uid.orEmpty()

    val courses = combine(
        courseRepository.getCourses(),
        courseRepository.getAllLessons(),
        if (userId.isBlank()) flowOf(emptyList()) else lessonProgressRepository.observeProgressForUser(userId),
        appSettingsStore.selectedLanguage()
    ) { modules, lessons, progress, language ->
        val progressByLesson = progress.associateBy { it.lessonId }
        modules.map { module ->
            buildModuleCard(
                module = module,
                lessons = lessons.filter { it.courseId == module.courseId }.sortedBy { it.orderIndex },
                progressByLesson = progressByLesson,
                language = language
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            courseRepository.refreshCourses()
        }
    }

    private fun buildModuleCard(
        module: CachedCourseEntity,
        lessons: List<CachedLessonEntity>,
        progressByLesson: Map<String, com.codeprep.app.data.local.entity.LessonProgressEntity>,
        language: String
    ): ModuleCardUi {
        val completedLessons = lessons.count { lesson -> progressByLesson[lesson.lessonId]?.completed == true }
        val perfectLessons = lessons.count { lesson -> progressByLesson[lesson.lessonId]?.perfectRun == true }
        val continueLesson = lessons.firstOrNull { lesson ->
            isLessonUnlocked(lesson, lessons, progressByLesson) &&
                progressByLesson[lesson.lessonId]?.completed != true
        } ?: lessons.lastOrNull()

        return ModuleCardUi(
            courseId = module.courseId,
            title = module.title.resolve(language),
            description = module.description.resolve(language),
            icon = module.icon,
            isLocked = module.isLocked,
            lessonCount = module.lessonCount,
            completedLessons = completedLessons,
            perfectLessons = perfectLessons,
            continueLessonId = continueLesson?.lessonId,
            continueLessonTitle = continueLesson?.title?.resolve(language)
        )
    }

    private fun isLessonUnlocked(
        lesson: CachedLessonEntity,
        sortedLessons: List<CachedLessonEntity>,
        progressByLesson: Map<String, com.codeprep.app.data.local.entity.LessonProgressEntity>
    ): Boolean {
        val index = sortedLessons.indexOfFirst { it.lessonId == lesson.lessonId }
        if (index <= 0) return true
        val previousLessonId = sortedLessons[index - 1].lessonId
        return progressByLesson[previousLessonId]?.completed == true
    }
}

package com.codeprep.app.ui.ai

import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.domain.model.SavedAiConversationSummary

data class ExplanationsModuleUi(
    val courseId: String,
    val title: String,
    val lessons: List<ExplanationLessonUi>
)

data class ExplanationLessonUi(
    val lessonId: String,
    val title: String,
    val preview: String
)

fun buildExplanationsModules(
    courses: List<CachedCourseEntity>,
    lessons: List<CachedLessonEntity>,
    savedConversations: List<SavedAiConversationSummary>,
    languageCode: String
): List<ExplanationsModuleUi> {
    val savedByLessonId = savedConversations.associateBy { it.lessonId }
    val lessonsByCourseId = lessons
        .sortedBy { it.orderIndex }
        .groupBy { it.courseId }

    return courses
        .sortedBy { it.orderIndex }
        .mapNotNull { course ->
            val savedLessons = lessonsByCourseId[course.courseId]
                .orEmpty()
                .mapNotNull { lesson ->
                    val summary = savedByLessonId[lesson.lessonId] ?: return@mapNotNull null
                    ExplanationLessonUi(
                        lessonId = lesson.lessonId,
                        title = lesson.title.resolve(languageCode),
                        preview = summary.preview.toPreviewExcerpt()
                    )
                }

            if (savedLessons.isEmpty()) {
                null
            } else {
                ExplanationsModuleUi(
                    courseId = course.courseId,
                    title = course.title.resolve(languageCode),
                    lessons = savedLessons
                )
            }
        }
}

private fun String.toPreviewExcerpt(): String {
    return replace(Regex("```[\\s\\S]*?```"), " ")
        .replace(Regex("`([^`]*)`"), "$1")
        .replace(Regex("!\\[[^\\]]*]\\(([^)]+)\\)"), " ")
        .replace(Regex("\\[([^\\]]+)]\\(([^)]+)\\)"), "$1")
        .replace(Regex("(?m)^\\s{0,3}#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s{0,3}[-*+]\\s+"), "")
        .replace(Regex("(?m)^\\s{0,3}>\\s?"), "")
        .replace("**", "")
        .replace("__", "")
        .replace("*", "")
        .replace("_", "")
        .replace(Regex("\\s+"), " ")
        .trim()
}

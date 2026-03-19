package com.codeprep.app.ui.ai

import com.codeprep.app.data.local.entity.CachedCourseEntity
import com.codeprep.app.data.local.entity.CachedLessonEntity
import com.codeprep.app.data.model.LessonContentBlock
import com.codeprep.app.data.model.LocalizedText
import com.codeprep.app.domain.model.SavedAiConversationSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class ExplanationsArchiveBuilderTest {

    @Test
    fun buildExplanationsModules_keepsCourseAndLessonOrder_andFiltersUnsavedLessons() {
        val modules = buildExplanationsModules(
            courses = listOf(
                course(courseId = "arrays", orderIndex = 0),
                course(courseId = "trees", orderIndex = 1)
            ),
            lessons = listOf(
                lesson(lessonId = "two-sum", courseId = "arrays", orderIndex = 0),
                lesson(lessonId = "anagram", courseId = "arrays", orderIndex = 1),
                lesson(lessonId = "depth", courseId = "trees", orderIndex = 0)
            ),
            savedConversations = listOf(
                summary(lessonId = "depth", preview = "Tree preview"),
                summary(lessonId = "anagram", preview = "Anagram preview")
            ),
            languageCode = "en"
        )

        assertEquals(listOf("arrays", "trees"), modules.map { it.courseId })
        assertEquals(listOf("anagram"), modules.first().lessons.map { it.lessonId })
        assertEquals("Anagram preview", modules.first().lessons.first().preview)
        assertEquals(listOf("depth"), modules.last().lessons.map { it.lessonId })
    }

    @Test
    fun buildExplanationsModules_resolvesLocalizedTitles_forSelectedLanguage() {
        val modules = buildExplanationsModules(
            courses = listOf(course(courseId = "arrays", orderIndex = 0)),
            lessons = listOf(lesson(lessonId = "two-sum", courseId = "arrays", orderIndex = 0)),
            savedConversations = listOf(summary(lessonId = "two-sum", preview = "Sacuvan pregled")),
            languageCode = "sr"
        )

        assertEquals("Nizovi", modules.single().title)
        assertEquals("Dva zbira", modules.single().lessons.single().title)
        assertEquals("Sacuvan pregled", modules.single().lessons.single().preview)
    }

    @Test
    fun buildExplanationsModules_formatsMarkdownPreviewIntoPlainExcerpt() {
        val modules = buildExplanationsModules(
            courses = listOf(course(courseId = "arrays", orderIndex = 0)),
            lessons = listOf(lesson(lessonId = "two-sum", courseId = "arrays", orderIndex = 0)),
            savedConversations = listOf(
                summary(
                    lessonId = "two-sum",
                    preview = """
                        ## Core idea
                        Use **complements** in a `HashMap`.

                        ```kotlin
                        val seen = mutableMapOf<Int, Int>()
                        ```
                    """.trimIndent()
                )
            ),
            languageCode = "en"
        )

        assertEquals(
            "Core idea Use complements in a HashMap.",
            modules.single().lessons.single().preview
        )
    }

    @Test
    fun buildExplanationsModules_hidesModuleWhenItsOnlySavedLessonIsRemoved() {
        val modules = buildExplanationsModules(
            courses = listOf(
                course(courseId = "arrays", orderIndex = 0),
                course(courseId = "trees", orderIndex = 1)
            ),
            lessons = listOf(
                lesson(lessonId = "two-sum", courseId = "arrays", orderIndex = 0),
                lesson(lessonId = "depth", courseId = "trees", orderIndex = 0)
            ),
            savedConversations = listOf(summary(lessonId = "depth", preview = "Tree preview")),
            languageCode = "en"
        )

        assertEquals(listOf("trees"), modules.map { it.courseId })
        assertEquals(listOf("depth"), modules.single().lessons.map { it.lessonId })
    }

    private fun course(courseId: String, orderIndex: Int): CachedCourseEntity {
        return CachedCourseEntity(
            courseId = courseId,
            title = LocalizedText(en = when (courseId) {
                "arrays" -> "Arrays"
                "trees" -> "Trees"
                else -> courseId
            }, sr = when (courseId) {
                "arrays" -> "Nizovi"
                "trees" -> "Stabla"
                else -> courseId
            }),
            description = LocalizedText(),
            orderIndex = orderIndex,
            isLocked = false,
            lessonCount = 0,
            icon = ""
        )
    }

    private fun lesson(lessonId: String, courseId: String, orderIndex: Int): CachedLessonEntity {
        return CachedLessonEntity(
            lessonId = lessonId,
            courseId = courseId,
            title = LocalizedText(en = when (lessonId) {
                "two-sum" -> "Two Sum"
                "anagram" -> "Valid Anagram"
                "depth" -> "Max Depth"
                else -> lessonId
            }, sr = when (lessonId) {
                "two-sum" -> "Dva zbira"
                "anagram" -> "Ispravan anagram"
                "depth" -> "Maksimalna dubina"
                else -> lessonId
            }),
            orderIndex = orderIndex,
            xpReward = 0,
            questionCount = 0,
            content = LessonContentBlock(),
            analogy = null,
            keyPoints = emptyList(),
            commonMistakes = emptyList(),
            codeSnippets = emptyList()
        )
    }

    private fun summary(lessonId: String, preview: String): SavedAiConversationSummary {
        return SavedAiConversationSummary(
            id = "conversation-$lessonId",
            lessonId = lessonId,
            courseTitle = "",
            lessonTitle = "",
            preview = preview,
            messageCount = 6,
            updatedAt = 0L
        )
    }
}

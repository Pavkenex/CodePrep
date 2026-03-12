package com.codeprep.app.domain

import com.codeprep.app.domain.model.LessonContext

object AiPromptBuilder {
    fun buildPrompt(question: String, lessonContext: LessonContext?): String {
        return if (lessonContext != null) {
            """
            You are a helpful programming tutor for CS students.
            The student is currently studying "${lessonContext.courseTitle} > ${lessonContext.lessonTitle}".
            
            Lesson summary:
            ${lessonContext.theorySummary}
            
            The student asks:
            "$question"
            
            Explain clearly and concisely with one practical example when useful.
            Respond in the same language as the student question.
            """.trimIndent()
        } else {
            """
            You are a helpful programming tutor for CS students.
            
            The student asks:
            "$question"
            
            Explain clearly and concisely with one practical example when useful.
            Respond in the same language as the student question.
            """.trimIndent()
        }
    }
}

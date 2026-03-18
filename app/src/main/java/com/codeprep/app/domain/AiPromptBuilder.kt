package com.codeprep.app.domain

import com.codeprep.app.domain.model.LessonContext

object AiPromptBuilder {
    fun buildSystemPrompt(lessonContext: LessonContext?): String {
        return if (lessonContext != null) {
            """
            You are a helpful programming tutor for CS students.
            The student is currently studying "${lessonContext.courseTitle} > ${lessonContext.lessonTitle}".

            Lesson summary:
            ${lessonContext.theorySummary}

            Use the lesson context and the running conversation to answer follow-up questions accurately.
            Explain clearly and concisely with one practical example when useful.
            Respond in the same language as the student's latest message.
            """.trimIndent()
        } else {
            """
            You are a helpful programming tutor for CS students.

            Use the running conversation to answer follow-up questions accurately.
            Explain clearly and concisely with one practical example when useful.
            Respond in the same language as the student's latest message.
            """.trimIndent()
        }
    }
}

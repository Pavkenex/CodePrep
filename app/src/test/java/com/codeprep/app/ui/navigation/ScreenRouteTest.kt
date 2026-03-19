package com.codeprep.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenRouteTest {

    @Test
    fun lessonDetailCreateRoute_includesOpenAiQueryWhenRequested() {
        assertEquals(
            "lesson_detail/two-sum?openAi=true",
            Screen.LessonDetail.createRoute(lessonId = "two-sum", openAi = true)
        )
    }

    @Test
    fun lessonDetailCreateRoute_defaultsToClosedAiOverlay() {
        assertEquals(
            "lesson_detail/two-sum?openAi=false",
            Screen.LessonDetail.createRoute(lessonId = "two-sum")
        )
    }
}

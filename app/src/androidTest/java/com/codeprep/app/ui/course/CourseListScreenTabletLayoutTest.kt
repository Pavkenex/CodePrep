package com.codeprep.app.ui.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.CodePrepTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CourseListScreenTabletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tabletLayout_centersBoundedCourseColumn() {
        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(900.dp)
                        .height(1200.dp)
                ) {
                    CourseListScreenContent(
                        courses = sampleModules(),
                        layout = courseListLayoutFor(screenWidthDp = 900),
                        onCourseClick = {}
                    )
                }
            }
        }

        val layout = courseListLayoutFor(screenWidthDp = 900)
        val container = composeTestRule.onNodeWithTag(COURSE_LIST_CONTAINER_TAG).assertExists()
        val root = composeTestRule.onNodeWithTag(COURSE_LIST_ROOT_TAG).assertExists()

        val containerBounds = container.fetchSemanticsNode().boundsInRoot
        val rootBounds = root.fetchSemanticsNode().boundsInRoot
        val containerCenter = (containerBounds.left + containerBounds.right) / 2f
        val rootCenter = (rootBounds.left + rootBounds.right) / 2f
        val tolerance = with(composeTestRule.density) { 2.dp.toPx() }
        val expectedContainerWidth = with(composeTestRule.density) {
            layout.maxContainerWidthDp.dp.toPx()
        }

        assertTrue(abs(containerBounds.width - expectedContainerWidth) <= tolerance)
        assertTrue(abs(containerCenter - rootCenter) <= tolerance)
    }
}

private fun sampleModules(): List<ModuleCardUi> {
    return listOf(
        ModuleCardUi(
            courseId = "module-1",
            title = "Foundations",
            description = "Learn the basics with a practical walkthrough.",
            icon = "FX",
            isLocked = false,
            lessonCount = 8,
            completedLessons = 3,
            perfectLessons = 2,
            continueLessonId = "lesson-4",
            continueLessonTitle = "Working with values"
        ),
        ModuleCardUi(
            courseId = "module-2",
            title = "Patterns",
            description = "Practice structuring code with reusable patterns.",
            icon = "PT",
            isLocked = true,
            lessonCount = 6,
            completedLessons = 0,
            perfectLessons = 0,
            continueLessonId = null,
            continueLessonTitle = null
        )
    )
}

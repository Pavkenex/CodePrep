package com.codeprep.app.ui.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.CardinalRed
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.CodePrepTheme
import com.codeprep.app.ui.theme.SunYellow
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

    @Test
    fun tabletHeaderSpacing_comesFromLayoutSpec() {
        val tabletLayout = courseListLayoutFor(screenWidthDp = 900)

        val header = measureHeaderBounds(
            widthDp = 900,
            layout = tabletLayout
        )

        val tolerance = with(composeTestRule.density) { 2.dp.toPx() }
        val expectedTabletTitleTop = with(composeTestRule.density) {
            tabletLayout.titleTopPaddingDp.dp.toPx()
        }

        assertTrue(abs(header.titleTop - expectedTabletTitleTop) <= tolerance)
        assertTrue(header.subtitleTop > header.titleTop)
    }

    @Test
    fun pillMetadata_wrapsWhenLabelsAreLong() {
        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(240.dp)
                ) {
                    ModuleJourneyCardPills(
                        pills = listOf(
                            CourseCardPill(
                                text = "A very long lessons label that should wrap",
                                accent = ElectricCyan
                            ),
                            CourseCardPill(
                                text = "An equally long stars label that should wrap",
                                accent = SunYellow
                            ),
                            CourseCardPill(
                                text = "A long progress label that should wrap too",
                                accent = CardinalRed
                            )
                        ),
                        layout = courseListLayoutFor(screenWidthDp = 900)
                    )
                }
            }
        }

        val firstPill = composeTestRule
            .onNodeWithText("A very long lessons label that should wrap")
            .assertExists()
        val secondPill = composeTestRule
            .onNodeWithText("An equally long stars label that should wrap")
            .assertExists()

        val firstBounds = firstPill.fetchSemanticsNode().boundsInRoot
        val secondBounds = secondPill.fetchSemanticsNode().boundsInRoot

        assertTrue(secondBounds.top > firstBounds.top)
    }

    private fun measureHeaderBounds(
        widthDp: Int,
        layout: CourseListLayoutSpec
    ): HeaderBounds {
        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(widthDp.dp)
                        .height(1200.dp)
                ) {
                    CourseListScreenContent(
                        courses = sampleModules(),
                        layout = layout,
                        onCourseClick = {}
                    )
                }
            }
        }

        val title = composeTestRule.onNodeWithTag(COURSE_LIST_TITLE_TAG).assertExists()
        val subtitle = composeTestRule.onNodeWithTag(COURSE_LIST_SUBTITLE_TAG).assertExists()

        return HeaderBounds(
            titleTop = title.fetchSemanticsNode().boundsInRoot.top,
            subtitleTop = subtitle.fetchSemanticsNode().boundsInRoot.top
        )
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

private data class HeaderBounds(
    val titleTop: Float,
    val subtitleTop: Float
)

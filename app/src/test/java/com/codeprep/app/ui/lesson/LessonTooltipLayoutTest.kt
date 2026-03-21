package com.codeprep.app.ui.lesson

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonTooltipLayoutTest {

    @Test
    fun computeTooltipLayout_keepsLastLessonTooltipBelow_andRequestsScrollWhenNeeded() {
        val layout = computeTooltipLayout(
            nodeBounds = Rect(
                offset = Offset(504f, 1240f),
                size = Size(72f, 72f)
            ),
            lessonIndex = 5,
            lessonCount = 6,
            rootSize = IntSize(width = 1080, height = 1400),
            tooltipSize = IntSize.Zero,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.BELOW, layout.placement)
        assertTrue(layout.scrollAdjustmentPx > 0)
    }
}

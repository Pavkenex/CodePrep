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
    fun computeTooltipLayout_keepsPhoneTooltipBelow_andRequestsScrollWhenNeeded() {
        val layout = computeTooltipLayout(
            nodeBounds = Rect(
                offset = Offset(504f, 1240f),
                size = Size(72f, 72f)
            ),
            lessonIndex = 5,
            lessonCount = 6,
            rootSize = IntSize(width = 1080, height = 1400),
            tooltipSize = IntSize.Zero,
            isTabletLayout = false,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.BELOW, layout.placement)
        assertTrue(layout.scrollAdjustmentPx > 0)
    }

    @Test
    fun computeTooltipLayout_keepsFirstTabletTooltipBelow() {
        val layout = computeTooltipLayout(
            nodeBounds = Rect(
                offset = Offset(320f, 320f),
                size = Size(72f, 72f)
            ),
            lessonIndex = 0,
            lessonCount = 6,
            rootSize = IntSize(width = 1280, height = 1800),
            tooltipSize = IntSize.Zero,
            isTabletLayout = true,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.BELOW, layout.placement)
    }

    @Test
    fun computeTooltipLayout_zigZagsTabletTooltipFromSecondNode() {
        val secondNodeLayout = computeTooltipLayout(
            nodeBounds = Rect(
                offset = Offset(440f, 520f),
                size = Size(72f, 72f)
            ),
            lessonIndex = 1,
            lessonCount = 6,
            rootSize = IntSize(width = 1280, height = 1800),
            tooltipSize = IntSize.Zero,
            isTabletLayout = true,
            density = Density(1f)
        )
        val thirdNodeLayout = computeTooltipLayout(
            nodeBounds = Rect(
                offset = Offset(620f, 720f),
                size = Size(72f, 72f)
            ),
            lessonIndex = 2,
            lessonCount = 6,
            rootSize = IntSize(width = 1280, height = 1800),
            tooltipSize = IntSize.Zero,
            isTabletLayout = true,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.LEFT, secondNodeLayout.placement)
        assertEquals(TooltipPlacement.RIGHT, thirdNodeLayout.placement)
    }

    @Test
    fun computeTooltipLayout_offsetsTabletLeftTooltipFartherSidewaysAndHigherThanNodeCentering() {
        val nodeBounds = Rect(
            offset = Offset(440f, 520f),
            size = Size(72f, 72f)
        )

        val layout = computeTooltipLayout(
            nodeBounds = nodeBounds,
            lessonIndex = 1,
            lessonCount = 6,
            rootSize = IntSize(width = 1280, height = 1800),
            tooltipSize = IntSize.Zero,
            isTabletLayout = true,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.LEFT, layout.placement)
        assertTrue(
            "Expected left tooltip to sit farther left from the node.",
            layout.offset.x <= 220
        )
        assertTrue(
            "Expected left tooltip to be lifted above the old centered position.",
            layout.offset.y <= 420
        )
    }

    @Test
    fun computeTooltipLayout_keepsTabletSideTooltipCloserAndRaisesPointerAnchor() {
        val nodeBounds = Rect(
            offset = Offset(440f, 520f),
            size = Size(72f, 72f)
        )
        val layout = computeTooltipLayout(
            nodeBounds = nodeBounds,
            lessonIndex = 1,
            lessonCount = 6,
            rootSize = IntSize(width = 1280, height = 1800),
            tooltipSize = IntSize.Zero,
            isTabletLayout = true,
            density = Density(1f)
        )

        assertEquals(TooltipPlacement.LEFT, layout.placement)
        assertTrue(
            "Expected tablet side tooltip to stay a bit closer to the node.",
            layout.offset.x >= 175
        )
    }

    @Test
    fun tooltipPointerTrianglePoints_aimsSidePointersTowardTheNode() {
        val rightPoints = tooltipPointerTrianglePoints(
            placement = TooltipPlacement.RIGHT,
            pointerCenter = 40f,
            canvasSize = Size(width = 5f, height = 120f),
            triangleHalfWidth = 5.5f
        )
        val leftPoints = tooltipPointerTrianglePoints(
            placement = TooltipPlacement.LEFT,
            pointerCenter = 40f,
            canvasSize = Size(width = 5f, height = 120f),
            triangleHalfWidth = 5.5f
        )

        assertEquals(
            "Right-side tooltip should point left toward the node.",
            0f,
            rightPoints.first().x
        )
        assertEquals(
            "Left-side tooltip should point right toward the node.",
            5f,
            leftPoints.first().x
        )
    }
}

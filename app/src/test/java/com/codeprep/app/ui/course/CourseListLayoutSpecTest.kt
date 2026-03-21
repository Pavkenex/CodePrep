package com.codeprep.app.ui.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseListLayoutSpecTest {

    @Test
    fun width599_keepsFullWidthSingleColumnBehavior() {
        val layout = courseListLayoutFor(screenWidthDp = 599)

        assertFalse(layout.useTabletContainer)
        assertEquals(16, layout.screenPaddingDp)
        assertEquals(16, layout.verticalSpacingDp)
        assertEquals(24, layout.titleTopPaddingDp)
        assertEquals(8, layout.titleBottomPaddingDp)
        assertEquals(20, layout.subtitleBottomPaddingDp)
        assertTrue(layout.maxContainerWidthDp == 0)
    }

    @Test
    fun width600_usesBoundedCenteredContainerAndStrongerHeaderSpacing() {
        val layout = courseListLayoutFor(screenWidthDp = 600)

        assertTrue(layout.useTabletContainer)
        assertEquals(40, layout.screenPaddingDp)
        assertEquals(24, layout.verticalSpacingDp)
        assertEquals(40, layout.titleTopPaddingDp)
        assertEquals(16, layout.titleBottomPaddingDp)
        assertEquals(32, layout.subtitleBottomPaddingDp)
        assertEquals(800, layout.maxContainerWidthDp)
    }
}

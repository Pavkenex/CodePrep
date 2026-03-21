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
        assertTrue(layout.maxContainerWidthDp == 0)
    }

    @Test
    fun width600_usesBoundedCenteredContainer() {
        val layout = courseListLayoutFor(screenWidthDp = 600)

        assertTrue(layout.useTabletContainer)
        assertEquals(40, layout.screenPaddingDp)
        assertEquals(24, layout.verticalSpacingDp)
        assertEquals(800, layout.maxContainerWidthDp)
    }
}

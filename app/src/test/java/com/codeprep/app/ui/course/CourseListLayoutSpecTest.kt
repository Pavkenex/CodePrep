package com.codeprep.app.ui.course

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseListLayoutSpecTest {

    @Test
    fun phoneWidth_keepsFullWidthSingleColumnBehavior() {
        val layout = courseListLayoutFor(screenWidthDp = 411)

        assertFalse(layout.useTabletContainer)
        assertTrue(layout.maxContainerWidthDp == 0)
    }

    @Test
    fun tabletWidth_usesBoundedCenteredContainer() {
        val layout = courseListLayoutFor(screenWidthDp = 800)

        assertTrue(layout.useTabletContainer)
        assertTrue(layout.maxContainerWidthDp in 780..800)
        assertTrue(layout.screenPaddingDp in 32..40)
    }
}

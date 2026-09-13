package com.codeprep.app.ui.course

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseListLayoutSpecTest {

    @Test
    fun width599_keepsFullWidthSingleColumnBehavior() {
        val layout = courseListLayoutFor(screenWidthDp = 599, screenHeightDp = 891)

        assertFalse(layout.useTabletContainer)
        assertEquals(16, layout.screenPaddingDp)
        assertEquals(16, layout.verticalSpacingDp)
        assertEquals(24, layout.titleTopPaddingDp)
        assertEquals(8, layout.titleBottomPaddingDp)
        assertEquals(20, layout.subtitleBottomPaddingDp)
        assertTrue(layout.maxContainerWidthDp == 0)
        assertEquals(18, layout.cardPaddingDp)
        assertEquals(14, layout.cardSectionSpacingDp)
        assertEquals(8, layout.pillHorizontalSpacingDp)
        assertEquals(8, layout.pillVerticalSpacingDp)
    }

    @Test
    fun width600_usesBoundedCenteredContainerAndStrongerHeaderSpacing() {
        val layout = courseListLayoutFor(screenWidthDp = 600, screenHeightDp = 1280)

        assertTrue(layout.useTabletContainer)
        assertEquals(40, layout.screenPaddingDp)
        assertEquals(24, layout.verticalSpacingDp)
        assertEquals(40, layout.titleTopPaddingDp)
        assertEquals(16, layout.titleBottomPaddingDp)
        assertEquals(32, layout.subtitleBottomPaddingDp)
        assertEquals(800, layout.maxContainerWidthDp)
        assertEquals(22, layout.cardPaddingDp)
        assertEquals(18, layout.cardSectionSpacingDp)
        assertEquals(10, layout.pillHorizontalSpacingDp)
        assertEquals(10, layout.pillVerticalSpacingDp)
    }

    @Test
    fun landscapePhone_keepsBoundedContainerButCompactsHeader() {
        val layout = courseListLayoutFor(screenWidthDp = 915, screenHeightDp = 412)

        assertTrue(layout.useTabletContainer)
        assertEquals(24, layout.screenPaddingDp)
        assertEquals(12, layout.verticalSpacingDp)
        assertEquals(8, layout.titleTopPaddingDp)
        assertEquals(4, layout.titleBottomPaddingDp)
        assertEquals(8, layout.subtitleBottomPaddingDp)
        assertEquals(800, layout.maxContainerWidthDp)
        assertEquals(16, layout.cardPaddingDp)
        assertEquals(12, layout.cardSectionSpacingDp)
        assertEquals(8, layout.pillHorizontalSpacingDp)
        assertEquals(8, layout.pillVerticalSpacingDp)
    }
}

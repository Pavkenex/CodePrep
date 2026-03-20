package com.codeprep.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenLayoutTest {

    @Test
    fun compactWidth_keepsSingleColumnHomeLayout() {
        val spec = homeScreenLayoutFor(screenWidthDp = 411)

        assertFalse(spec.useSplitLayout)
        assertEquals(16, spec.screenPaddingDp)
        assertEquals(24, spec.verticalSpacingDp)
        assertEquals(0, spec.maxContainerWidthDp)
        assertEquals(0, spec.railWidthDp)
        assertEquals(0, spec.contentWidthDp)
        assertEquals(0, spec.columnGapDp)
        assertEquals(0, spec.launchButtonWidthDp)
    }

    @Test
    fun widthBelowTabletBreakpoint_keepsSingleColumnHomeLayout() {
        val spec = homeScreenLayoutFor(screenWidthDp = 599)

        assertFalse(spec.useSplitLayout)
    }

    @Test
    fun tabletWidth_switchesToBalancedSplitLayout() {
        val spec = homeScreenLayoutFor(screenWidthDp = 600)

        assertTrue(spec.useSplitLayout)
        assertEquals(40, spec.screenPaddingDp)
        assertEquals(24, spec.verticalSpacingDp)
        assertEquals(520, spec.maxContainerWidthDp)
        assertEquals(220, spec.railWidthDp)
        assertEquals(280, spec.contentWidthDp)
        assertEquals(20, spec.columnGapDp)
        assertEquals(220, spec.launchButtonWidthDp)
    }

    @Test
    fun wideTablet_keepsTheSameCappedTabletLayoutTokens() {
        val spec = homeScreenLayoutFor(screenWidthDp = 980)

        assertTrue(spec.useSplitLayout)
        assertEquals(800, spec.maxContainerWidthDp)
        assertEquals(260, spec.railWidthDp)
        assertEquals(520, spec.contentWidthDp)
        assertEquals(20, spec.columnGapDp)
        assertEquals(220, spec.launchButtonWidthDp)
    }

    @Test
    fun tabletWidth_keepsSupportingRailNarrowerThanPrimaryContent() {
        val spec = homeScreenLayoutFor(screenWidthDp = 840)

        assertTrue(spec.useSplitLayout)
        assertTrue(spec.railWidthDp < spec.contentWidthDp)
        assertEquals(220, spec.launchButtonWidthDp)
    }
}

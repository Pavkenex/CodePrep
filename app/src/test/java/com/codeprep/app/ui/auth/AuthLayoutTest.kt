package com.codeprep.app.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthLayoutTest {

    @Test
    fun compactWidth_keepsPhoneStyleLayout() {
        val spec = authScreenLayoutFor(screenWidthDp = 411, screenHeightDp = 891)

        assertFalse(spec.useCenteredCard)
        assertEquals(24, spec.screenPaddingDp)
        assertEquals(24, spec.verticalPaddingDp)
        assertEquals(0, spec.cardWidthDp)
        assertEquals(0, spec.cardHorizontalPaddingDp)
        assertEquals(0, spec.cardVerticalPaddingDp)
        assertEquals(0, spec.formWidthDp)
        assertEquals(48, spec.brandBottomPaddingDp)
    }

    @Test
    fun tabletWidth_switchesToCenteredCardLayout() {
        val spec = authScreenLayoutFor(screenWidthDp = 840, screenHeightDp = 1280)

        assertTrue(spec.useCenteredCard)
        assertEquals(72, spec.screenPaddingDp)
        assertEquals(48, spec.verticalPaddingDp)
        assertEquals(320, spec.cardWidthDp)
        assertEquals(40, spec.cardHorizontalPaddingDp)
        assertEquals(44, spec.cardVerticalPaddingDp)
        assertEquals(280, spec.formWidthDp)
        assertEquals(32, spec.brandBottomPaddingDp)
    }

    @Test
    fun landscapePhoneHeight_usesCompactCenteredCardLayout() {
        val spec = authScreenLayoutFor(screenWidthDp = 915, screenHeightDp = 412)

        assertTrue(spec.useCenteredCard)
        assertEquals(16, spec.screenPaddingDp)
        assertEquals(12, spec.verticalPaddingDp)
        assertEquals(420, spec.cardWidthDp)
        assertEquals(28, spec.cardHorizontalPaddingDp)
        assertEquals(20, spec.cardVerticalPaddingDp)
        assertEquals(360, spec.formWidthDp)
        assertEquals(16, spec.brandBottomPaddingDp)
    }

    @Test
    fun narrowLandscapePhone_compactsBrandSpacingOnly() {
        val spec = authScreenLayoutFor(screenWidthDp = 533, screenHeightDp = 320)

        assertFalse(spec.useCenteredCard)
        assertEquals(24, spec.verticalPaddingDp)
        assertEquals(24, spec.brandBottomPaddingDp)
    }
}

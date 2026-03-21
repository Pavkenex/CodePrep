package com.codeprep.app.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthLayoutTest {

    @Test
    fun compactWidth_keepsPhoneStyleLayout() {
        val spec = authScreenLayoutFor(screenWidthDp = 411)

        assertFalse(spec.useCenteredCard)
        assertEquals(24, spec.screenPaddingDp)
        assertEquals(24, spec.verticalPaddingDp)
        assertEquals(0, spec.cardWidthDp)
        assertEquals(0, spec.cardHorizontalPaddingDp)
        assertEquals(0, spec.cardVerticalPaddingDp)
        assertEquals(0, spec.formWidthDp)
    }

    @Test
    fun tabletWidth_switchesToCenteredCardLayout() {
        val spec = authScreenLayoutFor(screenWidthDp = 840)

        assertTrue(spec.useCenteredCard)
        assertEquals(72, spec.screenPaddingDp)
        assertEquals(48, spec.verticalPaddingDp)
        assertEquals(320, spec.cardWidthDp)
        assertEquals(40, spec.cardHorizontalPaddingDp)
        assertEquals(44, spec.cardVerticalPaddingDp)
        assertEquals(280, spec.formWidthDp)
    }
}

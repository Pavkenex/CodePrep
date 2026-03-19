package com.codeprep.app.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TypographyTokensTest {

    @Test
    fun bodyLarge_usesApprovedReadingScale() {
        assertEquals(16f, AppTypography.bodyLarge.fontSize.value)
        assertEquals(25f, AppTypography.bodyLarge.lineHeight.value)
        assertEquals(FontWeight.Normal, AppTypography.bodyLarge.fontWeight)
    }

    @Test
    fun headlineLarge_usesApprovedExpressiveScale() {
        assertEquals(30f, AppTypography.headlineLarge.fontSize.value)
        assertEquals(36f, AppTypography.headlineLarge.lineHeight.value)
        assertEquals(FontWeight.Bold, AppTypography.headlineLarge.fontWeight)
    }

    @Test
    fun monoAccentStyle_usesDifferentFontFamily() {
        assertNotEquals(
            AppTypography.labelMedium.fontFamily,
            AppCodeTypography.labelMedium.fontFamily
        )
    }

    @Test
    fun typography_usesDedicatedAppFontFamilies() {
        assertSame(AppSansFontFamily, AppTypography.bodyLarge.fontFamily)
        assertSame(AppMonoFontFamily, AppCodeTypography.labelMedium.fontFamily)
        assertNotEquals(AppSansFontFamily, AppMonoFontFamily)
    }

    @Test
    fun expandedTypography_relaxesTopHeadlineSizes() {
        assertEquals(44f, AppExpandedTypography.displayMedium.fontSize.value)
        assertEquals(34f, AppExpandedTypography.headlineLarge.fontSize.value)
        assertEquals(29f, AppExpandedTypography.headlineMedium.fontSize.value)
        assertEquals(24f, AppExpandedTypography.headlineSmall.fontSize.value)
        assertEquals(16f, AppExpandedTypography.bodyLarge.fontSize.value)
    }
}

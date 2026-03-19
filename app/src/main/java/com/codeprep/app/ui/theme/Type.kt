package com.codeprep.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.codeprep.app.R

val AppSansFontFamily = FontFamily(
    Font(R.font.public_sans_regular, FontWeight.Normal),
    Font(R.font.public_sans_semibold, FontWeight.SemiBold),
    Font(R.font.public_sans_bold, FontWeight.Bold)
)

val AppMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

val AppCodeTypography = Typography(
    headlineLarge = AppTypography.headlineLarge.copy(fontFamily = AppMonoFontFamily),
    headlineMedium = AppTypography.headlineMedium.copy(fontFamily = AppMonoFontFamily),
    titleMedium = AppTypography.titleMedium.copy(fontFamily = AppMonoFontFamily),
    titleSmall = AppTypography.titleSmall.copy(fontFamily = AppMonoFontFamily),
    bodyMedium = AppTypography.bodyMedium.copy(fontFamily = AppMonoFontFamily),
    labelLarge = AppTypography.labelLarge.copy(fontFamily = AppMonoFontFamily),
    labelMedium = AppTypography.labelMedium.copy(fontFamily = AppMonoFontFamily),
    labelSmall = AppTypography.labelSmall.copy(fontFamily = AppMonoFontFamily),
    bodySmall = AppTypography.bodySmall.copy(fontFamily = AppMonoFontFamily)
)

val AppExpandedTypography = AppTypography.copy(
    displayMedium = AppTypography.displayMedium.copy(fontSize = 44.sp, lineHeight = 50.sp),
    headlineLarge = AppTypography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = AppTypography.headlineMedium.copy(fontSize = 29.sp, lineHeight = 35.sp),
    headlineSmall = AppTypography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 30.sp)
)

val Typography = AppTypography

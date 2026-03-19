package com.codeprep.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = IceWhite,
    tertiary = ElectricCyan,
    background = TrueBlack,
    surface = Charcoal,
    onPrimary = TrueBlack,
    onSecondary = TrueBlack,
    onTertiary = TrueBlack,
    onBackground = ElectricCyan,
    onSurface = IceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricCyan,
    secondary = Charcoal,
    tertiary = ElectricCyan,
    background = IceWhite,
    surface = Color.White,
    onPrimary = Charcoal,
    onSecondary = ElectricCyan,
    onTertiary = Charcoal,
    onBackground = Charcoal,
    onSurface = Charcoal
)

@Composable
fun CodePrepTheme(
    darkTheme: Boolean = true, // Force Dark Mode for Neon Terminal theme
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = if (configuration.screenWidthDp >= 600) {
        AppExpandedTypography
    } else {
        AppTypography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

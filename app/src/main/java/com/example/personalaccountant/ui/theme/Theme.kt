package com.example.personalaccountant.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = LightPrimary,
    onPrimary        = LightOnPrimary,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF00210E),
    secondary        = LightSecondary,
    onSecondary      = LightOnSecondary,
    tertiary         = LightTertiary,
    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = SurfaceVariantLt,
    onBackground     = Color(0xFF101C17),
    onSurface        = Color(0xFF101C17),
    onSurfaceVariant = Color(0xFF404944),
    error            = Color(0xFFB00020),
    outline          = BorderGray,
)

private val DarkColorScheme = darkColorScheme(
    primary          = DarkPrimary,
    onPrimary        = DarkOnPrimary,
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFF96F7B4),
    secondary        = DarkSecondary,
    onSecondary      = DarkOnSecondary,
    tertiary         = DarkTertiary,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = SurfaceVariantDk,
    onBackground     = Color(0xFFE2E3E1),
    onSurface        = Color(0xFFE2E3E1),
    onSurfaceVariant = Color(0xFFC0C9C3),
    error            = Color(0xFFCF6679),
    outline          = BorderGrayDark,
)

@Composable
fun PersonalAccountantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Respects system preference
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

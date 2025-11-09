package com.app.mindcycle.ui.theme

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
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = SoftPink,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryPink,
    onSecondary = TextPrimary,
    secondaryContainer = LightPink,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentPink,
    onTertiary = Color.White,
    tertiaryContainer = SoftPink,
    onTertiaryContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = BackgroundLight,
    onSurface = TextPrimary,
    surfaceVariant = LightPink,
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPink,
    onPrimary = Color.White,
    primaryContainer = DarkPink,
    onPrimaryContainer = Color.White,
    secondary = SecondaryPink,
    onSecondary = Color.White,
    secondaryContainer = AccentPink,
    onSecondaryContainer = Color.White,
    tertiary = SoftPink,
    onTertiary = TextPrimary,
    tertiaryContainer = DarkPink,
    onTertiaryContainer = Color.White,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = BackgroundDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3D2B3D),
    onSurfaceVariant = Color.White
)

@Composable
fun MindCycleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

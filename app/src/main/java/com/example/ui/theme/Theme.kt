package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PulsePrimary,
    onPrimary = Color.White,
    primaryContainer = PulsePrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = PulseSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0x3300F2FE),
    onSecondaryContainer = PulseSecondary,
    tertiary = PulseTertiary,
    onTertiary = Color.White,
    background = PulseDarkBackground,
    onBackground = PulseTextPrimaryDark,
    surface = PulseDarkSurface,
    onSurface = PulseTextPrimaryDark,
    surfaceVariant = PulseDarkSurfaceVariant,
    onSurfaceVariant = PulseTextSecondaryDark,
    outline = PulseDarkBorder,
    outlineVariant = PulseGlassHighlight
)

private val LightColorScheme = lightColorScheme(
    primary = PulsePrimary,
    onPrimary = Color.White,
    primaryContainer = PulsePrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = PulseSecondary,
    onSecondary = Color.Black,
    tertiary = PulseTertiary,
    background = PulseLightBackground,
    onBackground = PulseTextPrimaryLight,
    surface = PulseLightSurface,
    onSurface = PulseTextPrimaryLight,
    surfaceVariant = PulseLightSurfaceVariant,
    onSurfaceVariant = PulseTextSecondaryLight,
    outline = PulseLightBorder
)

@Composable
fun PulseTheme(
    darkTheme: Boolean = true, // Default to sleek dark video theme
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep legacy alias for backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PulseTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


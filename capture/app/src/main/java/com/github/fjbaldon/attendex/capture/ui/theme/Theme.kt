package com.github.fjbaldon.attendex.capture.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Neutral50,
    onPrimary = Neutral900,

    secondary = Neutral200,
    onSecondary = Neutral900,

    // === MAPPING SUCCESS TO TERTIARY ===
    tertiary = SemanticGreen,
    onTertiary = Neutral50,

    // === MAPPING WARNING TO ERROR CONTAINER ===
    errorContainer = SemanticOrange,
    onErrorContainer = Neutral50,

    error = SemanticRed,
    onError = Neutral50,

    background = Neutral900,
    onBackground = Neutral50,
    surface = Neutral900,
    onSurface = Neutral50,
    surfaceVariant = Neutral900,
    onSurfaceVariant = Neutral200,
    outline = Neutral200
)

private val LightColorScheme = lightColorScheme(
    primary = Neutral900,
    onPrimary = Neutral50,

    secondary = Neutral50,
    onSecondary = Neutral900,

    // === MAPPING SUCCESS TO TERTIARY ===
    tertiary = SemanticGreen,
    onTertiary = Neutral50,

    // === MAPPING WARNING TO ERROR CONTAINER ===
    errorContainer = SemanticOrange,
    onErrorContainer = Neutral50,

    error = SemanticRed,
    onError = Neutral50,

    background = Neutral50,
    onBackground = Neutral900,
    surface = Neutral50,
    onSurface = Neutral900,
    surfaceVariant = Neutral50,
    onSurfaceVariant = Neutral900,
    outline = Neutral200
)

@Composable
fun AttendExTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

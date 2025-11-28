package com.github.fjbaldon.attendex.capture.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// === SHAPES (From previous step) ===
private val BrandShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

private val DarkColorScheme = darkColorScheme(
    // In Dark Mode: Primary buttons are White text on Black background
    primary = BrandOffWhite,
    onPrimary = BrandBlack,

    // Secondary buttons (Muted)
    secondary = Slate800,
    onSecondary = BrandOffWhite,

    // Backgrounds
    background = BrandBlack,
    onBackground = BrandOffWhite,

    // Cards & Sheets
    surface = BrandBlack,
    onSurface = BrandOffWhite,

    // Inputs & Borders
    surfaceVariant = Slate800, // Input background
    onSurfaceVariant = Color.Gray, // Placeholder text
    outline = Slate700,        // Input borders

    // Semantic
    error = SemanticRed,
    onError = BrandOffWhite,
    tertiary = SemanticGreen,
    errorContainer = SemanticOrange
)

private val LightColorScheme = lightColorScheme(
    // In Light Mode: Primary buttons are Black text on White background (Monochrome)
    primary = BrandBlack,
    onPrimary = BrandWhite,

    // Secondary buttons (Muted)
    secondary = Slate100,
    onSecondary = BrandBlack,

    // Backgrounds
    background = BrandWhite,
    onBackground = BrandBlack,

    // Cards & Sheets
    surface = BrandWhite,
    onSurface = BrandBlack,

    // Inputs & Borders
    surfaceVariant = Slate100, // Input background
    onSurfaceVariant = Color.Gray, // Placeholder text
    outline = Slate200,        // Input borders

    // Semantic
    error = SemanticRed,
    onError = BrandWhite,
    tertiary = SemanticGreen,
    errorContainer = SemanticOrange
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
        shapes = BrandShapes,
        content = content
    )
}

package com.tsaha.nucleus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Gemini-inspired vibrant, blended palette
private val BlueGem = Color(0xFF3861FB)    // Gemini main blue
private val PurpleGem = Color(0xFF9846F0)  // Gemini vivid purple
private val GreenGem = Color(0xFF0BAF73)   // Gemini accent green
private val YellowGem = Color(0xFFFFC727)  // Gemini yellow/orange
private val SkyGem = Color(0xFFE7F0FF)     // Gemini gentle blue/white
private val Inky = Color(0xFF24243A)       // Deep blue-black blend
private val Night = Color(0xFF101017)      // Near black
private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF000000)

// Set primary = Purple in dark theme, Green in light theme. Blue and Yellow remain for secondary/tertiary.
private val DarkColorScheme = darkColorScheme(
    primary = PurpleGem,                        // Non-blue primary
    onPrimary = White,                          // White text/icons
    primaryContainer = BlueGem,                 // Vibrant blue container
    onPrimaryContainer = White,

    secondary = GreenGem,                       // Gemini green accent
    onSecondary = White,
    secondaryContainer = BlueGem,               // Bluish container
    onSecondaryContainer = White,

    tertiary = YellowGem,                       // Popping yellow
    onTertiary = Night,
    tertiaryContainer = PurpleGem,              // For cards/sheets
    onTertiaryContainer = White,

    background = Inky,                          // Deep space for backgrounds
    onBackground = SkyGem,                      // Gentle sky contrast

    surface = Night,                            // Even darker surface
    onSurface = White,
    surfaceVariant = Inky,                      // Tone variant
    onSurfaceVariant = SkyGem,

    outline = PurpleGem.copy(alpha = 0.5f),     // Translucent purple border
    outlineVariant = GreenGem.copy(alpha = 0.35f),

    error = Color(0xFFFF5252),                  // Default error red
    onError = Night,
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = White,
)

private val LightColorScheme = lightColorScheme(
    primary = GreenGem,                         // Non-blue primary
    onPrimary = White,                          // White text/icons
    primaryContainer = SkyGem,                  // Soft sky/gradient
    onPrimaryContainer = GreenGem,              // Green text/icons

    secondary = PurpleGem,                      // Purple accent
    onSecondary = White,
    secondaryContainer = BlueGem.copy(alpha = 0.13f),
    onSecondaryContainer = BlueGem,

    tertiary = YellowGem,                       // Gemini yellow
    onTertiary = Night,
    tertiaryContainer = Color(0xFFFFF8E1),      // Soft light orange
    onTertiaryContainer = YellowGem,

    background = SkyGem,                        // Light blue-sky background
    onBackground = Inky,                        // Strong dark text

    surface = White,                            // Clean white
    onSurface = Inky,                           // Blue-black text
    surfaceVariant = Color(0xFFEFE6FB),         // Light purple blend
    onSurfaceVariant = PurpleGem,

    outline = GreenGem.copy(alpha = 0.5f),      // Vibrant green border
    outlineVariant = YellowGem.copy(alpha = 0.29f),

    error = Color(0xFFD32F2F),                  // Standard error
    onError = White,
    errorContainer = Color(0xFFFFCDD2),         // Light error
    onErrorContainer = Color(0xFFB71C1C),
)

@Composable
fun NucleusTheme(
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
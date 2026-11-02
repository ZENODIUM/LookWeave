package com.vizeno.loomie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAFC2B8),
    onPrimary = DarkPaper,
    secondary = Brass,
    tertiary = Clay,
    background = Color(0xFF202522),
    surface = DarkPaper,
    onBackground = Parchment,
    onSurface = Parchment,
    outline = SoftInk
)

private val LightColorScheme = lightColorScheme(
    primary = Olive,
    onPrimary = Paper,
    secondary = Brass,
    tertiary = Brass,
    background = Parchment,
    surface = Paper,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = OliveLight,
    onSurfaceVariant = Ink,
    primaryContainer = OliveLight,
    onPrimaryContainer = Ink,
    secondaryContainer = OliveLight,
    onSecondaryContainer = Ink,
    tertiaryContainer = OliveLight,
    onTertiaryContainer = Ink,
    outline = Hairline
)

@Composable
fun LoomieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TikVideoDarkColorScheme = darkColorScheme(
    primary = TikCyan,
    onPrimary = Color(0xFF070A0F),
    primaryContainer = TikTealGreen,
    onPrimaryContainer = Color(0xFF070A0F),
    secondary = TikTealGreen,
    onSecondary = Color(0xFF070A0F),
    background = TikBackground,
    onBackground = TikTextPrimary,
    surface = TikSurface,
    onSurface = TikTextPrimary,
    surfaceVariant = TikChipInactive,
    onSurfaceVariant = TikTextSecondary,
    outline = TikDivider
)

@Composable
fun TikVideoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TikVideoDarkColorScheme,
        typography = Typography,
        content = content
    )
}

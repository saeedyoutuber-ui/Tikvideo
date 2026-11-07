package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// TikVideo Dark Theme Colors (matching screenshots)
val TikBackground = Color(0xFF0D0F17)
val TikSurface = Color(0xFF131824)
val TikSurfaceElevated = Color(0xFF1A2234)
val TikCard = Color(0xFF161C2A)
val TikChipInactive = Color(0xFF1E2536)
val TikSearchInput = Color(0xFF171D2A)

// TikVideo Signature Teal / Cyan Gradient & Accents
val TikTealGreen = Color(0xFF1DE9B6)
val TikCyan = Color(0xFF00E5FF)
val TikBlueCyan = Color(0xFF00B0FF)
val TikActiveGreen = Color(0xFF1DE9B6)
val TikHeartRed = Color(0xFFFF2D55)

// Text & Icon Colors
val TikTextPrimary = Color(0xFFFFFFFF)
val TikTextSecondary = Color(0xFF94A3B8)
val TikTextTertiary = Color(0xFF64748B)
val TikNavInactive = Color(0xFF94A3B8)
val TikDivider = Color(0xFF1B2232)

// Primary Gradient for Logo and Buttons
val TikGradientBrush = Brush.horizontalGradient(
    colors = listOf(TikTealGreen, TikBlueCyan)
)

val TikGlowBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
)

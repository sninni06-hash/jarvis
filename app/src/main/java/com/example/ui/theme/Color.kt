package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sci-Fi JARVIS / MYRAA Futuristic Palette
val JarvisCyan = Color(0xFF00F0FF)
val JarvisCyanDim = Color(0xFF0891B2)
val JarvisBlue = Color(0xFF38BDF8)
val JarvisDeepBg = Color(0xFF070B14)
val JarvisCardBg = Color(0xFF0F172A)
val JarvisCardBorder = Color(0xFF1E293B)
val JarvisGold = Color(0xFFFBBF24)
val JarvisRed = Color(0xFFEF4444)
val JarvisGreen = Color(0xFF10B981)
val JarvisTextPrimary = Color(0xFFF8FAFC)
val JarvisTextSecondary = Color(0xFF94A3B8)
val JarvisTextTertiary = Color(0xFF64748B)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisBlue,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = JarvisGold,
    onTertiary = Color(0xFF451A03),
    background = JarvisDeepBg,
    onBackground = JarvisTextPrimary,
    surface = JarvisCardBg,
    onSurface = JarvisTextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = JarvisTextSecondary,
    outline = Color(0xFF334155),
    error = JarvisRed,
    onError = Color.White
)

val LightColorScheme = DarkColorScheme // Default to dark futuristic theme

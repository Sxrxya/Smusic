package com.smusic.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Background Colors ─────────────────────
val BgPrimary = Color(0xFF0A0E27)
val BgSurface = Color(0xFF0F1428)
val BgCard = Color(0xFF1A1F3A)
val BgCardElevated = Color(0xFF232D4D)

// ── Primary Accents ───────────────────────
val AccentPrimary = Color(0xFFFF2E63)
val AccentSecondary = Color(0xFFA8E6E8)
val AccentTertiary = Color(0xFFFFD60A)
val AccentPurple = Color(0xFFC147E9)

// ── Text Colors ───────────────────────────
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFC8D4F4)
val TextMuted = Color(0xFF7B85C4)
val TextBright = Color(0xFFFFD60A)

// ── Border Colors ─────────────────────────
val BorderPrimary = Color(0x4DFF2E63) // rgba(255, 46, 99, 0.3)
val BorderSecondary = Color(0x33A8E6E8) // rgba(168, 230, 232, 0.2)
val BorderGlow = Color(0x80FF2E63) // rgba(255, 46, 99, 0.5)
val BorderDivider = Color(0xFF303450)

// ── Gradients ─────────────────────────────
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF2E63), Color(0xFFFF006E), Color(0xFFC1121F))
)
val CyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFA8E6E8), Color(0xFF00D4FF), Color(0xFF0099FF))
)
val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFD60A), Color(0xFFFFC300), Color(0xFFFF9500))
)
val PurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFC147E9), Color(0xFF7B2FFF), Color(0xFF5A189A))
)
val OrangeGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF6B35), Color(0xFFF7931E), Color(0xFFFDB833))
)
val GreenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00D084), Color(0xFF00A86B), Color(0xFF228B22))
)
val PinkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF006E), Color(0xFFFB5607), Color(0xFFFFBE0B))
)

// ── Old definitions (for compatibility during transition) ──
val BackgroundPrimary = BgPrimary
val BackgroundSurface = BgSurface
val CardBackground = BgCard
val CardElevated = BgCardElevated
val AccentGold = AccentTertiary
val AccentTeal = AccentSecondary

// ── Featured Gradients (Updated to Gen Z palette) ──
val BollywoodGradient1 = Color(0xFFC1121F)
val BollywoodGradient2 = Color(0xFFFF006E)
val BollywoodGradient3 = Color(0xFFFF2E63)

val ChillGradient1 = Color(0xFF0099FF)
val ChillGradient2 = Color(0xFF00D4FF)
val ChillGradient3 = Color(0xFFA8E6E8)

val PunjabiGradient1 = Color(0xFFFF9500)
val PunjabiGradient2 = Color(0xFFFFC300)
val PunjabiGradient3 = Color(0xFFFFD60A)

val IndieGradient1 = Color(0xFF5A189A)
val IndieGradient2 = Color(0xFF7B2FFF)
val IndieGradient3 = Color(0xFFC147E9)

val RomanticGradient1 = Color(0xFFFF006E)
val RomanticGradient2 = Color(0xFFFB5607)
val RomanticGradient3 = Color(0xFFFFBE0B)

val PartyGradient1 = Color(0xFF00A86B)
val PartyGradient2 = Color(0xFF00D084)
val PartyGradient3 = Color(0xFF228B22)

// ── Status Colors ─────────────────────────
val Success = Color(0xFF00D084)
val Warning = Color(0xFFFFD60A)
val Error = Color(0xFFFF2E63)

// ── Shimmer ───────────────────────────────
val ShimmerBase = BgCard
val ShimmerHighlight = BgCardElevated

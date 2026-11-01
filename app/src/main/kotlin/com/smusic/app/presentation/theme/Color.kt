package com.smusic.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Apple & Amazon Hybrid Background Palette ─────────────────────
val BgPrimary = Color(0xFF07090E)        // Deep OLED obsidian void
val BgSurface = Color(0xFF0E121B)        // Frosted surface container
val BgCard = Color(0xFF141926)           // Translucent glass card base
val BgCardElevated = Color(0xFF1C2234)   // Elevated acrylic glass

// ── Hybrid Accents (Apple Music Rose + Amazon Music Cyan) ────────
val AccentPrimary = Color(0xFFFA233B)    // Apple Music signature Rose/Red
val AccentSecondary = Color(0xFF00E5FF)  // Amazon Music electric Cyan
val AccentTertiary = Color(0xFFFF9900)   // Amazon Music amber gold
val AccentPurple = Color(0xFFB537F2)     // Apple Music deep violet

// ── Text Colors ──────────────────────────────────────────────────
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFCBD5E1)
val TextMuted = Color(0xFF64748B)
val TextBright = Color(0xFFFFD60A)

// ── Glassmorphism & Border Styling ───────────────────────────────
val BorderPrimary = Color(0x40FA233B)    // Subtle Apple rose glow
val BorderSecondary = Color(0x3300E5FF)  // Subtle Amazon cyan glow
val BorderGlow = Color(0x66FA233B)
val BorderDivider = Color(0xFF1E2433)
val GlassSurface = Color(0x14FFFFFF)     // Frosted glass overlay

// ── Vibrant Hybrid Gradients ─────────────────────────────────────
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFA233B), Color(0xFFFF007A), Color(0xFFC1121F))
)
val CyanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF00A8E8), Color(0xFF0077B6))
)
val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF9900), Color(0xFFFFB703), Color(0xFFFB8500))
)
val PurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFB537F2), Color(0xFF7B2FFF), Color(0xFF5A189A))
)
val OrangeGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF6B35), Color(0xFFF7931E), Color(0xFFFDB833))
)
val GreenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF1B5E20))
)
val PinkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF007A), Color(0xFFFA233B), Color(0xFFFF85A1))
)
val HybridHeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFA233B), Color(0xFF00E5FF))
)

// ── Backwards Compatibility Aliases ──────────────────────────────
val BackgroundPrimary = BgPrimary
val BackgroundSurface = BgSurface
val CardBackground = BgCard
val CardElevated = BgCardElevated
val AccentGold = AccentTertiary
val AccentTeal = AccentSecondary

// ── Category Gradients ───────────────────────────────────────────
val BollywoodGradient1 = Color(0xFFC1121F)
val BollywoodGradient2 = Color(0xFFFF007A)
val BollywoodGradient3 = Color(0xFFFA233B)

val ChillGradient1 = Color(0xFF0077B6)
val ChillGradient2 = Color(0xFF00A8E8)
val ChillGradient3 = Color(0xFF00E5FF)

val PunjabiGradient1 = Color(0xFFFB8500)
val PunjabiGradient2 = Color(0xFFFFB703)
val PunjabiGradient3 = Color(0xFFFF9900)

val IndieGradient1 = Color(0xFF5A189A)
val IndieGradient2 = Color(0xFF7B2FFF)
val IndieGradient3 = Color(0xFFB537F2)

val RomanticGradient1 = Color(0xFFFF007A)
val RomanticGradient2 = Color(0xFFFB5607)
val RomanticGradient3 = Color(0xFFFFBE0B)

val PartyGradient1 = Color(0xFF00C853)
val PartyGradient2 = Color(0xFF00E676)
val PartyGradient3 = Color(0xFF69F0AE)

// ── Status Colors ────────────────────────────────────────────────
val Success = Color(0xFF00E676)
val Warning = Color(0xFFFF9900)
val Error = Color(0xFFFA233B)

// ── Shimmer ──────────────────────────────────────────────────────
val ShimmerBase = BgCard
val ShimmerHighlight = BgCardElevated

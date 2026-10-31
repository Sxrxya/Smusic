package com.smusic.app.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset

val SMusicShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// ── Circle ──────────────────────────────
val Circle = CircleShape

// ── Shadows ─────────────────────────────
val Elevation1 = Shadow(
    color = Color(0x4D000000), // rgba(0,0,0,0.3)
    offset = Offset(0f, 2f),
    blurRadius = 8f
)
val Elevation2 = Shadow(
    color = Color(0x66000000), // rgba(0,0,0,0.4)
    offset = Offset(0f, 4f),
    blurRadius = 16f
)
val Elevation3 = Shadow(
    color = Color(0x80000000), // rgba(0,0,0,0.5)
    offset = Offset(0f, 8f),
    blurRadius = 24f
)

val GlowPrimary = Shadow(
    color = Color(0x66FF2E63), // rgba(255, 46, 99, 0.4)
    offset = Offset(0f, 0f),
    blurRadius = 20f
)
val GlowSecondary = Shadow(
    color = Color(0x4DA8E6E8), // rgba(168, 230, 232, 0.3)
    offset = Offset(0f, 0f),
    blurRadius = 20f
)
val GlowGold = Shadow(
    color = Color(0x4DFFD60A), // rgba(255, 214, 10, 0.3)
    offset = Offset(0f, 0f),
    blurRadius = 16f
)

// ── Glass Effect Constants ──────────────
val GlassBackground = Color(0xB31A1F3A) // rgba(26, 31, 58, 0.7)
val GlassBlur = 10.dp
val GlassBorder = BorderPrimary

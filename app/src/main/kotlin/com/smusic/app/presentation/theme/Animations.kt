package com.smusic.app.presentation.theme

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.pulseAnimation(
    initialValue: Float = 1f,
    targetValue: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    return this.scale(scale)
}

@Composable
fun Modifier.glowAnimation(
    initialAlpha: Float = 0.3f,
    targetAlpha: Float = 0.6f,
    durationMillis: Int = 1500
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = initialAlpha,
        targetValue = targetAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    return this.graphicsLayer(alpha = alpha)
}

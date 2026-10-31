package com.smusic.app.presentation.splash

import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.smusic.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SplashIntro(
    onFinished: () -> Unit,
    onHit: () -> Unit,
    isShortVersion: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    
    val animatorScale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
    if (animatorScale == 0f) {
        SideEffect { onFinished() }
        return
    }

    val durationMult = if (isShortVersion) 0.5f else 1.0f
    
    val dotScale = remember { Animatable(0f) }
    val barHeights = List(5) { remember { Animatable(0f) } }
    val morphProgress = remember { Animatable(0f) }
    val ringScale = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val wordmarkY = remember { Animatable(12f) }
    val taglineAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }
    
    val particles = remember { List(12) { Particle() } }
    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        dotScale.animateTo(1f, tween(300))
        
        delay((300 * durationMult).toLong())
        barHeights.forEachIndexed { index, anim ->
            launch {
                delay(index * 60L)
                anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
        
        delay((600 * durationMult).toLong())
        morphProgress.animateTo(1f, tween(300))
        
        delay((300 * durationMult).toLong())
        onHit()
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        
        launch {
            flashAlpha.snapTo(0.3f)
            flashAlpha.animateTo(0f, tween(300))
        }
        launch {
            ringAlpha.snapTo(1f)
            ringScale.animateTo(1f, tween(600, easing = LinearOutSlowInEasing))
            ringAlpha.animateTo(0f, tween(400))
        }
        launch {
            particleProgress.animateTo(1f, tween(800))
        }
        
        delay((200 * durationMult).toLong())
        glowAlpha.animateTo(0.5f, tween(600))
        
        delay((300 * durationMult).toLong())
        launch { wordmarkAlpha.animateTo(1f, tween(400)) }
        launch { wordmarkY.animateTo(0f, tween(400)) }
        
        delay((400 * durationMult).toLong())
        taglineAlpha.animateTo(0.7f, tween(300))
        
        delay((300 * durationMult).toLong())
        screenAlpha.animateTo(0f, tween(250))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
            .alpha(screenAlpha.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                scope.launch {
                    screenAlpha.animateTo(0f, tween(150))
                    onFinished()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = flashAlpha.value)))

        Canvas(modifier = Modifier.size(300.dp)) {
            if (glowAlpha.value > 0) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF2E63).copy(alpha = glowAlpha.value), Color.Transparent),
                        center = center,
                        radius = size.width / 2
                    )
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (ringAlpha.value > 0) {
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(Color(0xFFFF2E63), Color(0xFFA8E6E8))),
                            radius = (size.minDimension / 2) * ringScale.value * 2.5f,
                            alpha = ringAlpha.value,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (particleProgress.value > 0 && particleProgress.value < 1) {
                        particles.forEach { p ->
                            val dist = p.speed * particleProgress.value * 300f
                            val x = center.x + Math.cos(p.angle) * dist
                            val y = center.y + Math.sin(p.angle) * dist
                            drawCircle(
                                color = p.color,
                                radius = 2.dp.toPx() * (1f - particleProgress.value),
                                center = Offset(x.toFloat(), y.toFloat())
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.size(100.dp)) {
                    val barWidth = 12.dp.toPx()
                    val spacing = 8.dp.toPx()
                    val totalWidth = (barWidth * 5) + (spacing * 4)
                    val startX = (size.width - totalWidth) / 2
                    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF2E63), Color(0xFFA8E6E8)))

                    if (morphProgress.value < 1f) {
                        for (i in 0 until 5) {
                            val x = startX + i * (barWidth + spacing)
                            val height = 60.dp.toPx() * barHeights[i].value
                            val alpha = 1f - morphProgress.value
                            
                            if (dotScale.value > 0 && barHeights[0].value == 0f) {
                                drawCircle(Color(0xFFFF2E63), radius = 6.dp.toPx() * dotScale.value, center = center)
                            } else {
                                drawRoundRect(
                                    brush = gradient,
                                    topLeft = Offset(x, center.y - height / 2),
                                    size = Size(barWidth, height),
                                    cornerRadius = CornerRadius(barWidth / 2),
                                    alpha = alpha
                                )
                            }
                        }
                    }

                    if (morphProgress.value > 0f) {
                        val sAlpha = morphProgress.value
                        for (i in 0 until 5) {
                            val x = startX + i * (barWidth + spacing)
                            drawBarSegmented(x, center.y, barWidth, 75.dp.toPx(), i, gradient, sAlpha)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SMUSIC",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(wordmarkAlpha.value).offset(y = wordmarkY.value.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.intro_tagline),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBarSegmented(
    x: Float, centerY: Float, width: Float, maxHeight: Float, index: Int, brush: Brush, alpha: Float
) {
    val top = centerY - maxHeight / 2
    val r = CornerRadius(width / 2)
    when (index) {
        0 -> {
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.08f), Size(width, maxHeight * 0.08f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.35f), Size(width, maxHeight * 0.55f), r, alpha)
        }
        1 -> {
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.05f), Size(width, maxHeight * 0.22f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.48f), Size(width, maxHeight * 0.25f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.85f), Size(width, maxHeight * 0.12f), r, alpha)
        }
        2 -> {
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.15f), Size(width, maxHeight * 0.18f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.55f), Size(width, maxHeight * 0.22f), r, alpha)
        }
        3 -> {
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.12f), Size(width, maxHeight * 0.25f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.58f), Size(width, maxHeight * 0.25f), r, alpha)
        }
        4 -> {
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.08f), Size(width, maxHeight * 0.55f), r, alpha)
            drawRoundRect(brush, Offset(x, top + maxHeight * 0.85f), Size(width, maxHeight * 0.08f), r, alpha)
        }
    }
}

class Particle {
    val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
    val speed = Random.nextDouble(0.5, 1.5)
    val color = if (Random.nextBoolean()) Color(0xFFFF2E63) else Color(0xFFA8E6E8)
}

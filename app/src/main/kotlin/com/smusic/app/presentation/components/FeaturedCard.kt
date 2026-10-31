package com.smusic.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.AccentSecondary
import com.smusic.app.presentation.theme.BgCard
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

data class FeaturedItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val gradientColors: List<Color>,
    val category: String,
)

@Composable
fun FeaturedCard(
    item: FeaturedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1f, label = "featured_scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(item.gradientColors))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .border(
                1.5.dp,
                Brush.verticalGradient(listOf(Color.White.copy(0.4f), Color.Transparent)),
                RoundedCornerShape(24.dp)
            ),
    ) {
        // Glass Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgCard.copy(alpha = 0.2f))
                .blur(15.dp)
        )

        // Emoji decoration
        Text(
            text = item.emoji,
            fontSize = 80.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            color = Color.White.copy(alpha = 0.25f),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = item.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.drawBehind {
                        // Subtle text shadow logic can be added here if needed
                    }
                )
                Text(
                    text = item.subtitle,
                    color = TextPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.category.uppercase(),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                )
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .drawBehind {
                            if (isHovered) {
                                drawCircle(
                                    color = item.gradientColors.last(),
                                    radius = size.maxDimension / 2f + 8.dp.toPx(),
                                    alpha = 0.6f
                                )
                            }
                        },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.3f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = TextPrimary,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp),
                    )
                }
            }
        }
    }
}

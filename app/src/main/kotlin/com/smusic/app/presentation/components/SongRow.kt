package com.smusic.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smusic.app.domain.model.Song
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.AccentSecondary
import com.smusic.app.presentation.theme.BorderPrimary
import com.smusic.app.presentation.theme.GlassBackground
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun SongRow(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
    index: Int? = null,
    showDuration: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else if (isHovered) 1.02f else 1f,
        label = "scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (index != null) {
            Text(
                text = "${index + 1}",
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.size(24.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .drawBehind {
                    drawCircle(
                        Brush.linearGradient(listOf(AccentPrimary, AccentSecondary)),
                        radius = size.maxDimension / 2f + 4.dp.toPx(),
                        alpha = 0.4f
                    )
                }
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(AccentPrimary, AccentSecondary)),
                        RoundedCornerShape(8.dp)
                    ),
                contentScale = ContentScale.Crop,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = song.title,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (showDuration && song.durationSeconds > 0) {
                    Text(
                        text = " • ${song.durationFormatted}",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        trailing?.invoke()

        if (onMoreClick != null) {
            IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

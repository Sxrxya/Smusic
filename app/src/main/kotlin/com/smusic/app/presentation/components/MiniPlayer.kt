package com.smusic.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smusic.app.domain.model.PlayerState
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.AccentSecondary
import com.smusic.app.presentation.theme.BgCard
import com.smusic.app.presentation.theme.BorderPrimary
import com.smusic.app.presentation.theme.PrimaryGradient
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = playerState.currentSong ?: return
    val progress by animateFloatAsState(
        targetValue = playerState.progress,
        animationSpec = tween(200),
        label = "mini_progress",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        AccentPrimary.copy(alpha = 0.1f),
                        AccentSecondary.copy(alpha = 0.1f)
                    )
                )
            )
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clickable { onClick() },
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = AccentPrimary,
            trackColor = TextMuted.copy(alpha = 0.2f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderPrimary, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = song.title,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = song.artist,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0x80FF2E63),
                            radius = size.maxDimension / 2f + 4.dp.toPx(),
                            alpha = 0.5f
                        )
                    }
                    .clip(CircleShape)
                    .background(PrimaryGradient)
            ) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

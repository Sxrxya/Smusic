package com.smusic.app.presentation.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.AccentSecondary
import com.smusic.app.presentation.theme.BgCard
import com.smusic.app.presentation.theme.BgPrimary
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSheet(
    lyrics: List<LyricLine>,
    isLoading: Boolean,
    currentPositionMs: Long,
    onDismiss: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    var isSyncedMode by remember { mutableStateOf(true) }

    // Find current lyric index based on active playback position
    val currentIndex = remember(lyrics, currentPositionMs) {
        val idx = lyrics.indexOfLast { it.timeMs <= currentPositionMs }
        if (idx == -1 && lyrics.isNotEmpty()) 0 else idx
    }

    // Auto-scroll to current line in Synced mode
    LaunchedEffect(currentIndex, isSyncedMode) {
        if (isSyncedMode && lyrics.isNotEmpty() && currentIndex >= 0) {
            val targetScroll = (currentIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(index = targetScroll)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color(0xFF0A0D14),
        scrimColor = Color.Black.copy(alpha = 0.75f),
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.92f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // JioSaavn / Apple Music style Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSyncedMode) Brush.horizontalGradient(listOf(AccentPrimary, AccentSecondary))
                                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { isSyncedMode = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = null,
                                tint = if (isSyncedMode) TextPrimary else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Karaoke",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSyncedMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSyncedMode) TextPrimary else TextMuted,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (!isSyncedMode) Brush.horizontalGradient(listOf(AccentPrimary, AccentSecondary))
                                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { isSyncedMode = false }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Notes,
                                contentDescription = null,
                                tint = if (!isSyncedMode) TextPrimary else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Plain",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (!isSyncedMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSyncedMode) TextPrimary else TextMuted,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentPrimary,
                                modifier = Modifier.size(42.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Fetching synced lyrics...", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                lyrics.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No lyrics found for this song.\nEnjoy the beat! 🎶",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(30.dp)) }

                        itemsIndexed(lyrics) { index, line ->
                            val isCurrent = isSyncedMode && (index == currentIndex)
                            val isPast = isSyncedMode && (index < currentIndex)

                            val scale by animateFloatAsState(
                                targetValue = if (isCurrent) 1.05f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                                label = "scaleAnimation"
                            )

                            val textColor by animateColorAsState(
                                targetValue = when {
                                    !isSyncedMode -> TextPrimary
                                    isCurrent -> Color.White
                                    isPast -> TextMuted.copy(alpha = 0.45f)
                                    else -> TextSecondary.copy(alpha = 0.65f)
                                },
                                animationSpec = tween(durationMillis = 200),
                                label = "colorAnimation"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCurrent) AccentPrimary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        if (line.timeMs >= 0) onSeek(line.timeMs)
                                    }
                                    .padding(vertical = if (isCurrent) 14.dp else 10.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = line.text,
                                    color = textColor,
                                    fontSize = if (isCurrent) 22.sp else 16.sp,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                                    lineHeight = if (isCurrent) 30.sp else 24.sp,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

package com.smusic.app.presentation.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.domain.model.RepeatMode
import com.smusic.app.presentation.theme.*

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val playerState by viewModel.playerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val song = playerState.currentSong

    LaunchedEffect(song) {
        song?.let { viewModel.loadLyrics(it) }
    }

    if (song == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Text("No song playing", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    // Lyrics sheet
    if (uiState.showLyrics) {
        LyricsSheet(
            lyrics = uiState.lyrics,
            isLoading = uiState.isLoadingLyrics,
            currentPositionMs = playerState.positionMs,
            onDismiss = { viewModel.toggleLyricsSheet() },
            onSeek = { viewModel.seekTo(it) },
        )
    }

    // Queue sheet
    if (uiState.showQueue) {
        QueueSheet(
            queue = playerState.queue,
            currentIndex = playerState.currentIndex,
            onDismiss = { viewModel.toggleQueueSheet() },
            onPlayIndex = { viewModel.serviceConnection.playFromQueue(it) },
            onRemove = { viewModel.serviceConnection.removeFromQueue(it) },
            onClearQueue = { viewModel.serviceConnection.clearQueue() },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AccentSecondary.copy(alpha = 0.15f),
                        BackgroundPrimary,
                        BackgroundPrimary,
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = TextPrimary, modifier = Modifier.size(28.dp))
                }
                Text("Now Playing", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, "More", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album art
            AsyncImage(
                model = song.highResThumbnail.ifBlank { song.thumbnailUrl },
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Song info
            Text(
                text = song.title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = AccentPrimary,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (song.album.isNotBlank()) {
                Text(
                    text = song.album,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.toggleLike(song) }) {
                    Icon(
                        if (uiState.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        "Like",
                        tint = if (uiState.isLiked) AccentPrimary else TextSecondary,
                    )
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.PlaylistAdd, "Add to playlist", tint = TextSecondary)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Download, "Download", tint = TextSecondary)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Share, "Share", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek bar
            var sliderPosition by remember(playerState.positionMs) {
                mutableFloatStateOf(playerState.positionMs.toFloat())
            }

            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = { viewModel.seekTo(sliderPosition.toLong()) },
                valueRange = 0f..playerState.durationMs.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentPrimary,
                    activeTrackColor = AccentPrimary,
                    inactiveTrackColor = BorderDivider,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(playerState.currentTimeFormatted, color = TextMuted, style = MaterialTheme.typography.labelMedium)
                Text(playerState.totalTimeFormatted, color = TextMuted, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        "Shuffle",
                        tint = if (playerState.shuffleEnabled) AccentPrimary else TextSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                IconButton(onClick = { viewModel.previous() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }

                // Play/Pause button
                Surface(
                    onClick = { viewModel.playPause() },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = AccentPrimary,
                ) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = TextPrimary,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                IconButton(onClick = { viewModel.next() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }

                IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                    Icon(
                        when (playerState.repeatMode) {
                            RepeatMode.REPEAT_ONE -> Icons.Filled.RepeatOne
                            else -> Icons.Filled.Repeat
                        },
                        "Repeat",
                        tint = when (playerState.repeatMode) {
                            RepeatMode.OFF -> TextSecondary
                            else -> AccentPrimary
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = { viewModel.toggleLyricsSheet() }) {
                    Icon(Icons.Filled.Lyrics, "Lyrics", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { viewModel.toggleQueueSheet() }) {
                    Icon(Icons.Filled.QueueMusic, "Queue", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Speed, "Speed", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { viewModel.toggleSleepTimer() }) {
                    Icon(Icons.Filled.Timer, "Timer", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Tune, "Equalizer", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
            }

            // Playing from
            if (playerState.playingFrom.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Playing from: ${playerState.playingFrom}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

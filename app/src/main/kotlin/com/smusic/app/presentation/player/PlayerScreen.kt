package com.smusic.app.presentation.player

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.domain.model.RepeatMode
import com.smusic.app.domain.model.Song
import androidx.compose.material.icons.filled.Bedtime
import com.smusic.app.presentation.components.SleepTimerSheet
import com.smusic.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToEqualizer: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val song = playerState.currentSong

    LaunchedEffect(song?.videoId) {
        song?.let { viewModel.loadLyrics(it) }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07090E)),
            contentAlignment = Alignment.Center,
        ) {
            Text("No track playing", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
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

    // Add to Playlist bottom sheet
    if (uiState.showAddToPlaylist) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleAddToPlaylistSheet() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF10141E),
        ) {
            var newPlaylistName by remember { mutableStateOf("") }
            var isCreating by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Add to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isCreating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("Playlist name", color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    viewModel.createPlaylistAndAddSong(newPlaylistName.trim(), song)
                                    isCreating = false
                                    newPlaylistName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Text("Create")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { isCreating = true }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = AccentPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("New Playlist", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.playlists.isEmpty() && !isCreating) {
                    Text(
                        "No playlists yet. Create one above!",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        items(uiState.playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.addSongToPlaylist(playlist.playlistId, song) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = AccentSecondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(playlist.name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep timer sheet
    if (uiState.showSleepTimer) {
        SleepTimerSheet(
            isActive = uiState.sleepTimerActive,
            remainingMinutes = uiState.sleepTimerRemaining,
            onDismiss = { viewModel.toggleSleepTimer() },
            onSelectTimer = { minutes ->
                viewModel.setSleepTimer(minutes)
                viewModel.toggleSleepTimer()
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
                viewModel.toggleSleepTimer()
            }
        )
    }

    // Fully opaque root container to eliminate any underlying backstack bleed-through
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E)),
    ) {
        // Frosted ambient glow background derived from album thumbnail
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
                .drawBehind {
                    drawRect(Color(0xFF07090E).copy(alpha = 0.72f))
                },
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
                Text(
                    "NOW PLAYING",
                    color = TextSecondary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.toggleSleepTimer() }) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (uiState.sleepTimerActive) Color(0xFF00E5FF) else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album Artwork with Keyed Fade Transition and Neon Glow Border
            AnimatedContent(
                targetState = song.videoId,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label = "AlbumArtAnimatedContent"
            ) { _ ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .drawBehind {
                            drawCircle(
                                Brush.linearGradient(listOf(Color(0xFFFA233B), Color(0xFF00E5FF))),
                                radius = size.maxDimension / 2f + 16.dp.toPx(),
                                alpha = 0.28f
                            )
                        }
                ) {
                    AsyncImage(
                        model = song.highResThumbnail.ifBlank { song.thumbnailUrl },
                        contentDescription = song.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(Color(0xFFFA233B), Color(0xFF00E5FF))),
                                RoundedCornerShape(26.dp)
                            ),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Song Info with Keyed Transition (Completely Eliminates Double Text Stacking Glitch)
            AnimatedContent(
                targetState = song,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(180)) },
                label = "SongMetadataAnimatedContent"
            ) { currentSong ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentSong.title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentSong.artist,
                        color = Color(0xFF00E5FF),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            onNavigateToArtist(currentSong.artistId.ifEmpty { currentSong.artist })
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action row (Favorite, Add to Playlist, Download, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Like button
                IconButton(onClick = { viewModel.toggleLike(song) }) {
                    Icon(
                        if (uiState.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        "Like",
                        tint = if (uiState.isLiked) AccentPrimary else TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Add to Playlist button
                IconButton(onClick = { viewModel.toggleAddToPlaylistSheet() }) {
                    Icon(Icons.Filled.PlaylistAdd, "Add to Playlist", tint = TextSecondary, modifier = Modifier.size(28.dp))
                }

                // Download button with round percentage indicator
                IconButton(onClick = { viewModel.downloadSong(song) }) {
                    when {
                        uiState.isDownloading -> {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (uiState.downloadProgress.coerceIn(1, 100)) / 100f },
                                    modifier = Modifier.size(32.dp),
                                    color = Color(0xFF00E5FF),
                                    trackColor = Color.White.copy(alpha = 0.15f),
                                    strokeWidth = 2.5.dp
                                )
                                Text(
                                    text = "${uiState.downloadProgress}%",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        uiState.isDownloaded -> {
                            Icon(Icons.Filled.CheckCircle, "Downloaded", tint = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                        }
                        else -> {
                            Icon(Icons.Filled.Download, "Download", tint = TextSecondary, modifier = Modifier.size(28.dp))
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            // Single unified interactive seek bar
            var sliderPosition by remember(playerState.currentSong?.videoId, playerState.positionMs) {
                mutableFloatStateOf(playerState.positionMs.toFloat())
            }

            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = { viewModel.seekTo(sliderPosition.toLong()) },
                valueRange = 0f..playerState.effectiveDurationMs.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFFA233B),
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(playerState.currentTimeFormatted, color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Text(playerState.totalTimeFormatted, color = TextMuted, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Controls row (Shuffle, Previous, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        "Shuffle",
                        tint = if (playerState.shuffleEnabled) AccentPrimary else TextSecondary,
                        modifier = Modifier.size(28.dp),
                    )
                }

                IconButton(onClick = { viewModel.previous() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", tint = TextPrimary, modifier = Modifier.size(40.dp))
                }

                // Apple/Amazon hybrid glowing Play/Pause button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .drawBehind {
                            drawCircle(Color(0xFFFA233B).copy(alpha = 0.25f), radius = 42.dp.toPx())
                            drawCircle(Color(0xFFFA233B).copy(alpha = 0.12f), radius = 50.dp.toPx())
                        }
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFA233B), Color(0xFFFF007A))))
                        .clickable { viewModel.playPause() }
                ) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = TextPrimary,
                        modifier = Modifier.size(44.dp),
                    )
                }

                IconButton(onClick = { viewModel.next() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.size(40.dp))
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
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Frosted Glassmorphism Secondary Controls (Lyrics, Queue, Sleep Timer, Equalizer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.toggleLyricsSheet() }) {
                    Icon(Icons.Filled.Lyrics, "Lyrics", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { viewModel.toggleQueueSheet() }) {
                    Icon(Icons.Filled.QueueMusic, "Queue", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { viewModel.toggleSleepTimer() }) {
                    Icon(Icons.Filled.Timer, "Timer", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onNavigateToEqualizer) {
                    Icon(Icons.Filled.Tune, "Equalizer", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
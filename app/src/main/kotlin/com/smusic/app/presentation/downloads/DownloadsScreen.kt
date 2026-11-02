package com.smusic.app.presentation.downloads

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.domain.model.Song
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text(
                    "Downloads",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (state.songs.isNotEmpty() || state.pendingDownloads.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.deleteAll()
                        Toast.makeText(context, "Deleted all downloads", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.DeleteSweep, "Delete All", tint = Color(0xFFFA233B))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Storage: ${viewModel.formatSize(state.totalSize)} • ${state.songs.size} offline tracks",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Active / Pending Downloads with Live Percentage
        if (state.pendingDownloads.isNotEmpty()) {
            item {
                Text(
                    text = "DOWNLOADING (${state.pendingDownloads.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(state.pendingDownloads) { dl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = dl.thumbnailUrl,
                        contentDescription = dl.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dl.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = dl.artist,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (dl.progress.coerceIn(0, 100)) / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = Color(0xFF00E5FF),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${dl.progress}%",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Ready for Offline Listening
        if (state.songs.isNotEmpty()) {
            item {
                Text(
                    text = "READY OFFLINE (${state.songs.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFA233B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(state.songs, key = { it.videoId }) { song ->
                DownloadedSongRowItem(
                    song = song,
                    onPlay = {
                        viewModel.serviceConnection.playSong(song, state.songs, "Downloads")
                        onNavigateToPlayer()
                    },
                    onDelete = {
                        viewModel.deleteDownload(song.videoId)
                        Toast.makeText(context, "Deleted from offline storage", Toast.LENGTH_SHORT).show()
                    },
                    onPlayNext = {
                        viewModel.playNext(song)
                        Toast.makeText(context, "Playing next", Toast.LENGTH_SHORT).show()
                    },
                    onAddToQueue = {
                        viewModel.addToQueue(song)
                        Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else if (state.pendingDownloads.isEmpty()) {
            item {
                EmptyState("No downloads yet. Download songs for 100% offline listening!")
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun DownloadedSongRowItem(
    song: Song,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        SongRow(
            song = song,
            onClick = onPlay,
            onMoreClick = { showMenu = true },
            trailing = {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Downloaded Offline",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(Color(0xFF131722))
        ) {
            DropdownMenuItem(
                text = { Text("Play Next", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.SkipNext, contentDescription = null, tint = Color(0xFF00E5FF)) },
                onClick = {
                    onPlayNext()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Add to Queue", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = Color(0xFF00E5FF)) },
                onClick = {
                    onAddToQueue()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Download", color = Color(0xFFFA233B)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFFA233B)) },
                onClick = {
                    onDelete()
                    showMenu = false
                }
            )
        }
    }
}

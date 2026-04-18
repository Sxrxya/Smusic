package com.smusic.app.presentation.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun PlaylistScreen(
    playlistId: Long,
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                text = state.playlist?.name ?: "Playlist",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = {
                if (state.songs.isNotEmpty()) {
                    viewModel.serviceConnection.playSong(state.songs.first(), state.songs, state.playlist?.name ?: "Playlist")
                    onNavigateToPlayer()
                }
            }) { Icon(Icons.Filled.PlayArrow, "Play", tint = AccentPrimary) }
            IconButton(onClick = {
                if (state.songs.isNotEmpty()) {
                    val shuffled = state.songs.shuffled()
                    viewModel.serviceConnection.playSong(shuffled.first(), shuffled, state.playlist?.name ?: "Playlist")
                    onNavigateToPlayer()
                }
            }) { Icon(Icons.Filled.Shuffle, "Shuffle", tint = AccentPrimary) }
        }

        Text(
            text = "${state.songCount} songs",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.songs.isEmpty()) {
            EmptyState("This playlist is empty")
        } else {
            LazyColumn {
                items(state.songs) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, state.playlist?.name ?: "Playlist")
                            onNavigateToPlayer()
                        },
                    )
                }
            }
        }
    }
}

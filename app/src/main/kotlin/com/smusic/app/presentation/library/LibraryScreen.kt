package com.smusic.app.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

val libraryTabs = listOf("Liked Songs", "Playlists", "Albums", "Artists", "History")

@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // Create playlist dialog
    if (state.showCreatePlaylistDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreatePlaylist() },
            title = { Text("Create Playlist", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Playlist name", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = BorderDivider,
                    ),
                )
            },
            confirmButton = {
                Button(
                    onClick = { if (name.isNotBlank()) viewModel.createPlaylist(name) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                ) { Text("Create", color = TextPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleCreatePlaylist() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BackgroundSurface,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Library", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
            Row {
                IconButton(onClick = onNavigateToDownloads) {
                    Icon(Icons.Filled.CloudDownload, "Downloads", tint = TextSecondary)
                }
                IconButton(onClick = onNavigateToStats) {
                    Icon(Icons.Filled.BarChart, "Stats", tint = TextSecondary)
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Color.Transparent,
            contentColor = AccentPrimary,
            edgePadding = 16.dp,
            divider = {},
        ) {
            libraryTabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            title,
                            color = if (state.selectedTab == index) AccentPrimary else TextMuted,
                        )
                    },
                )
            }
        }

        // Content
        when (state.selectedTab) {
            0 -> LikedSongsTab(state, viewModel, onNavigateToPlayer)
            1 -> PlaylistsTab(state, viewModel, onNavigateToPlaylist)
            2 -> AlbumsTab(state)
            3 -> ArtistsTab(state)
            4 -> HistoryTab(state, viewModel, onNavigateToPlayer)
        }
    }
}

@Composable
fun LikedSongsTab(state: LibraryUiState, viewModel: LibraryViewModel, onNavigateToPlayer: () -> Unit) {
    if (state.likedSongs.isEmpty()) {
        EmptyState("No liked songs yet. Tap ♥ on songs you love!")
    } else {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("${state.likedSongsCount} songs", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.playAll(state.likedSongs, "Liked Songs") }) {
                    Icon(Icons.Filled.PlayArrow, "Play All", tint = AccentPrimary)
                }
                IconButton(onClick = { viewModel.shuffleAll(state.likedSongs, "Liked Songs") }) {
                    Icon(Icons.Filled.Shuffle, "Shuffle", tint = AccentPrimary)
                }
            }
            LazyColumn {
                items(state.likedSongs) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, state.likedSongs, "Liked Songs")
                            onNavigateToPlayer()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistsTab(state: LibraryUiState, viewModel: LibraryViewModel, onNavigateToPlaylist: (Long) -> Unit) {
    Column {
        if (state.playlists.isEmpty()) {
            EmptyState("No playlists yet. Create one to get started!")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.playlists) { playlist ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .clickable { onNavigateToPlaylist(playlist.playlistId) }
                            .padding(12.dp),
                    ) {
                        Text(playlist.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${playlist.songCount} songs",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        // FAB
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            FloatingActionButton(
                onClick = { viewModel.toggleCreatePlaylist() },
                containerColor = AccentPrimary,
            ) {
                Icon(Icons.Filled.Add, "Create Playlist", tint = TextPrimary)
            }
        }
    }
}

@Composable
fun AlbumsTab(state: LibraryUiState) {
    if (state.albums.isEmpty()) {
        EmptyState("No saved albums")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.albums) { album ->
                com.smusic.app.presentation.components.AlbumCard(album = album, onClick = { })
            }
        }
    }
}

@Composable
fun ArtistsTab(state: LibraryUiState) {
    if (state.artists.isEmpty()) {
        EmptyState("No followed artists")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.artists) { artist ->
                com.smusic.app.presentation.components.ArtistCard(artist = artist, onClick = { })
            }
        }
    }
}

@Composable
fun HistoryTab(state: LibraryUiState, viewModel: LibraryViewModel, onNavigateToPlayer: () -> Unit) {
    if (state.history.isEmpty()) {
        EmptyState("No listening history yet. Start playing some music!")
    } else {
        Column {
            TextButton(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier.padding(horizontal = 16.dp),
            ) { Text("Clear History", color = Error) }

            LazyColumn {
                items(state.history) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, state.history, "History")
                            onNavigateToPlayer()
                        },
                    )
                }
            }
        }
    }
}

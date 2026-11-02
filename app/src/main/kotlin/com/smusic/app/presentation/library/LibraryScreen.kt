package com.smusic.app.presentation.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.domain.model.Playlist
import com.smusic.app.presentation.components.ArtistCard
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToStats: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("Playlists") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Create playlist dialog
    if (state.showCreatePlaylistDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreatePlaylist() },
            title = { Text("New Playlist", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Playlist name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = BorderDivider,
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createPlaylist(name.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleCreatePlaylist() }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF131722),
            shape = RoundedCornerShape(20.dp),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding(),
    ) {
        // Top Header: "Your Library", "+ Create", and Downloads / Stats quick pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Downloads pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { onNavigateToDownloads() }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.CloudDownload,
                        contentDescription = "Downloads",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Stats pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { onNavigateToStats() }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = "Listening Stats",
                        tint = Color(0xFFFA233B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // "+ Create" button matching Spotify / Apple design
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { viewModel.toggleCreatePlaylist() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Create",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Spotify-style Filter Pills
        val filterPills = listOf("Playlists", "Liked Songs", "Downloaded", "Artists", "Albums")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filterPills.forEach { pill ->
                val isSelected = selectedFilter == pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF00E5FF)
                            else Color.White.copy(alpha = 0.07f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.12f),
                            CircleShape
                        )
                        .clickable { selectedFilter = pill }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pill,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Subheader with search toggle and sorting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isSearching = !isSearching }
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search Library",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSearching) "Close Search" else "Filter",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Recents",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Menu,
                    contentDescription = "Sort",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = isSearching) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter your library…", color = TextMuted) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = BorderDivider,
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Tab content based on selected filter
        when (selectedFilter) {
            "Playlists" -> {
                val filteredPlaylists = state.playlists.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Always show Liked Songs hero row at top of Playlists
                    item {
                        LikedSongsHeroItem(
                            count = state.likedSongsCount,
                            onClick = { selectedFilter = "Liked Songs" }
                        )
                    }

                    if (filteredPlaylists.isEmpty() && state.playlists.isEmpty()) {
                        item {
                            EmptyState("No custom playlists yet. Tap '+ Create' to make your first playlist!")
                        }
                    } else {
                        items(filteredPlaylists, key = { it.playlistId }) { playlist ->
                            PlaylistItemRow(
                                playlist = playlist,
                                onClick = { onNavigateToPlaylist(playlist.playlistId) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }

            "Liked Songs" -> {
                val songs = state.likedSongs.filter {
                    searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
                }
                if (songs.isEmpty()) {
                    EmptyState("No liked songs yet. Tap ♥ on songs you love!")
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${songs.size} songs",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { viewModel.playAll(songs, "Liked Songs") }) {
                                    Icon(Icons.Filled.PlayArrow, "Play All", tint = Color(0xFFFA233B))
                                }
                                IconButton(onClick = { viewModel.shuffleAll(songs, "Liked Songs") }) {
                                    Icon(Icons.Filled.Shuffle, "Shuffle", tint = Color(0xFF00E5FF))
                                }
                            }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(songs, key = { it.videoId }) { song ->
                                SongRow(
                                    song = song,
                                    onClick = {
                                        viewModel.playSong(song, songs, "Liked Songs")
                                        onNavigateToPlayer()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            "Downloaded" -> {
                // Navigate to downloads screen directly
                LaunchedEffect(Unit) {
                    onNavigateToDownloads()
                }
            }

            "Artists" -> {
                if (state.artists.isEmpty()) {
                    EmptyState("No followed artists. Listen to more music to discover artists!")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.artists, key = { it.artistId }) { artist ->
                            ArtistCard(
                                artist = artist,
                                onClick = { /* Navigate artist */ }
                            )
                        }
                    }
                }
            }

            "Albums" -> {
                if (state.albums.isEmpty()) {
                    EmptyState("No saved albums yet.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.albums, key = { it.albumId }) { album ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardBackground)
                                    .padding(10.dp)
                            ) {
                                AsyncImage(
                                    model = album.thumbnailUrl,
                                    contentDescription = album.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    album.title,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    album.artist,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LikedSongsHeroItem(
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Glowing purple gradient container with white heart
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF7A42F4), Color(0xFF4C6EF5), Color(0xFF00E5FF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = "Liked Songs",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Liked Songs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📌 Playlist • $count songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (playlist.thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = playlist.thumbnailUrl,
                contentDescription = playlist.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Playlist • ${playlist.songCount} ${if (playlist.songCount == 1) "song" else "songs"}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

package com.smusic.app.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.domain.model.Song
import com.smusic.app.presentation.components.ArtistCard
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.ErrorState
import com.smusic.app.presentation.components.LanguageChips
import com.smusic.app.presentation.components.ShimmerSongRow
import com.smusic.app.presentation.components.SongCard
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

val languages = listOf("All", "Hindi", "English", "Tamil", "Telugu", "Punjabi", "Marathi", "Bengali")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilterPill by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 90.dp),
    ) {
        // Top Apple / Spotify Hybrid Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW & TRENDING",
                        color = Color(0xFFFA233B),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = state.greeting.replace(" 🎵", ""),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Filled.Search, "Search", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Spotify-style Filter Pills (All, Music, Trending, Party, Relax, Workout)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("All", "Music", "Trending", "Party", "Relax", "Workout").forEach { pill ->
                    val isSelected = selectedFilterPill == pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFFA233B)
                                else Color.White.copy(alpha = 0.07f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFFFA233B) else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .clickable {
                                selectedFilterPill = pill
                                if (pill != "All" && pill != "Music") {
                                    viewModel.onMoodSelected(pill)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pill,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // If mood filter is active, display the curated mood feed
        if (selectedFilterPill != "All" && selectedFilterPill != "Music") {
            val moodTracks = if (selectedFilterPill == "Trending") state.trendingSongs else state.moodSongs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$selectedFilterPill Mix",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Curated songs for your mood",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (moodTracks.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.playSong(moodTracks.first(), moodTracks, "$selectedFilterPill Mix")
                                onNavigateToPlayer()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFA233B))
                        ) {
                            Icon(Icons.Filled.PlayArrow, "Play All", tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            if (moodTracks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Curating $selectedFilterPill tracks…", color = TextSecondary)
                    }
                }
            } else {
                items(moodTracks, key = { "mood_${selectedFilterPill}_${it.videoId}" }) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, moodTracks, "$selectedFilterPill Mix")
                            onNavigateToPlayer()
                        }
                    )
                }
            }
        } else {
            // Default full home sections
        
        if (state.recentlyPlayed.isNotEmpty() || state.trendingSongs.isNotEmpty()) {
            item {
                val gridItems = (state.recentlyPlayed + state.trendingSongs).distinctBy { it.videoId }.take(6)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in gridItems.chunked(2)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { song ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .clickable {
                                            viewModel.playSong(song, gridItems, "Quick Access")
                                            onNavigateToPlayer()
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.thumbnailUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = song.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Apple Music Wide Featured Album / Playlist Banner Cards
        if (state.trendingSongs.isNotEmpty()) {
            item {
                val featuredSongs = state.trendingSongs.take(4)
                val bannerPagerState = rememberPagerState(pageCount = { featuredSongs.size })

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Featured Spotlight",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    HorizontalPager(
                        state = bannerPagerState,
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        pageSpacing = 12.dp,
                    ) { page ->
                        val item = featuredSongs[page]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                                .clickable {
                                    viewModel.playSong(item, featuredSongs, "Featured Spotlight")
                                    onNavigateToPlayer()
                                }
                        ) {
                            AsyncImage(
                                model = item.highResThumbnail.ifBlank { item.thumbnailUrl },
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient Vignette
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "FEATURED TRACK",
                                    color = Color(0xFF00E5FF),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.artist,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Floating Play Icon Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFA233B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Apple Music "Best New Songs >" Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCategory("Trending") }
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Best New Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "See All",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    )
                }
                Text("See All", color = Color(0xFFFA233B), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.isLoading) {
            items(6) { ShimmerSongRow() }
        } else if (state.error != null) {
            item { ErrorState(message = state.error ?: "Error", onRetry = viewModel::loadHome) }
        } else if (state.trendingSongs.isEmpty()) {
            item { EmptyState("No trending songs found") }
        } else {
            items(state.trendingSongs.take(8)) { song ->
                SongRow(
                    song = song,
                    onClick = {
                        viewModel.playSong(song, state.trendingSongs, "Best New Songs")
                        onNavigateToPlayer()
                    },
                    index = state.trendingSongs.indexOf(song),
                )
            }
        }

        // Top Artists with Glowing Ring Avatars
        if (state.topArtists.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Top Artists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.topArtists) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { if (artist.artistId.isNotBlank()) onNavigateToArtist(artist.artistId) },
                        )
                    }
                }
            }
        }

        // YouTube Music Featured Playlists
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Featured Playlists", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.featuredPlaylists) { playlist ->
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .size(width = 160.dp, height = 150.dp)
                            .clickable {
                                viewModel.playYouTubePlaylist(playlist)
                                onNavigateToPlayer()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        playlist.gradientColors.map { Color(it) }
                                    )
                                )
                                .padding(14.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.35f), shape = androidx.compose.foundation.shape.CircleShape)
                                        .padding(6.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = playlist.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = playlist.subtitle,
                                    color = Color.White.copy(alpha = 0.85f),
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

        // Jump Back In (Recently Played Cards)
        if (state.recentlyPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Jump Back In", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.recentlyPlayed.take(10)) { song ->
                        SongCard(
                            song = song,
                            onClick = {
                                viewModel.playSong(song, state.recentlyPlayed, "Jump Back In")
                                onNavigateToPlayer()
                            },
                        )
                    }
                }
            }
        }

        // Based on Your Taste
        if (state.basedOnTaste.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Based on Your Taste", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.basedOnTaste) { song ->
                        SongCard(
                            song = song,
                            onClick = {
                                viewModel.playSong(song, state.basedOnTaste, "Based on Taste")
                                onNavigateToPlayer()
                            },
                        )
                    }
                }
            }
        }

        // Language & Cultural Discovery
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Music by Language", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            LanguageChips(
                languages = languages,
                selectedLanguage = state.selectedLanguage,
                onLanguageSelected = viewModel::onLanguageSelected,
            )
        }
        }
    }
}

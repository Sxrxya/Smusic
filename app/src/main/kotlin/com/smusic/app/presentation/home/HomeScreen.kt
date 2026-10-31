package com.smusic.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.ArtistCard
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.ErrorState
import com.smusic.app.presentation.components.FeaturedCard
import com.smusic.app.presentation.components.FeaturedItem
import com.smusic.app.presentation.components.LanguageChips
import com.smusic.app.presentation.components.ShimmerSongRow
import com.smusic.app.presentation.components.SongCard
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*
import kotlinx.coroutines.delay

val languages = listOf("All", "Hindi", "English", "Tamil", "Telugu", "Punjabi", "Marathi", "Bengali")
val moodGradients = mapOf(
    "Happy" to GoldGradient,
    "Sad" to CyanGradient,
    "Party" to PinkGradient,
    "Focus" to PurpleGradient,
    "Workout" to OrangeGradient,
    "Sleep" to CyanGradient
)
val moods = listOf("Happy 😊", "Sad 😢", "Party 🎉", "Focus 🎯", "Workout 💪", "Sleep 😴")

val featuredItems = listOf(
    FeaturedItem(
        "Party Hits",
        "Best of 2024",
        "🔥",
        listOf(AccentPrimary, Color(0xFFFF006E)),
        "Party"
    ),
    FeaturedItem(
        "Chill Vibes",
        "Smooth Jazz",
        "🌙",
        listOf(Color(0xFF0099FF), Color(0xFFA8E6E8)),
        "Relax"
    ),
    FeaturedItem(
        "Workout Mix",
        "High Energy",
        "⚡",
        listOf(Color(0xFFFF6B35), Color(0xFFFDB833)),
        "Workout"
    )
)

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
    val pagerState = rememberPagerState(pageCount = { featuredItems.size })

    // Auto-scroll pager
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % featuredItems.size
            pagerState.animateScrollToPage(next)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .drawBehind {
                            // Text gradient can be applied here with Brush.linearGradient
                        },
                )
                Row {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Filled.Search, "Search", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Filled.Settings, "Settings", tint = TextSecondary)
                    }
                }
            }
        }

        // Language chips
        item {
            LanguageChips(
                languages = languages,
                selectedLanguage = state.selectedLanguage,
                onLanguageSelected = viewModel::onLanguageSelected,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mood chips
        item {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                moods.forEach { mood ->
                    val moodKey = mood.split(" ").first()
                    val isSelected = state.selectedMood == moodKey
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) Modifier.background(moodGradients[moodKey] ?: PrimaryGradient)
                                else Modifier
                                    .background(BgCard.copy(alpha = 0.3f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            )
                            .clickable { viewModel.onMoodSelected(moodKey) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = mood,
                            color = if (isSelected) Color.White else TextSecondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Featured pager
        item {
            SectionHeader("FEATURED")
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 24.dp),
                pageSpacing = 16.dp,
            ) { page ->
                FeaturedCard(
                    item = featuredItems[page],
                    onClick = { onNavigateToCategory(featuredItems[page].category) },
                )
            }

            // Dot indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(featuredItems.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    val width by animateFloatAsState(if (isSelected) 24f else 8f, label = "dot_width")
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AccentPrimary else TextMuted.copy(alpha = 0.5f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Top Artists
        if (state.topArtists.isNotEmpty()) {
            item {
                SectionHeader("TOP ARTISTS")
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Trending Now
        item { SectionHeader("TRENDING NOW") }

        if (state.isLoading) {
            items(8) { ShimmerSongRow() }
        } else if (state.error != null) {
            item { ErrorState(message = state.error ?: "Error", onRetry = viewModel::loadHome) }
        } else if (state.trendingSongs.isEmpty()) {
            item { EmptyState("No trending songs found") }
        } else {
            items(state.trendingSongs.take(8)) { song ->
                SongRow(
                    song = song,
                    onClick = {
                        viewModel.playSong(song, state.trendingSongs, "Trending Now")
                        onNavigateToPlayer()
                    },
                    index = state.trendingSongs.indexOf(song),
                )
            }
        }

        // Recently Played
        if (state.recentlyPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.recentlyPlayed.take(10)) { song ->
                        SongCard(
                            song = song,
                            onClick = {
                                viewModel.playSong(song, state.recentlyPlayed, "Recently Played")
                                onNavigateToPlayer()
                            },
                        )
                    }
                }
            }
        }

        // Mood songs
        if (state.moodSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("${state.selectedMood ?: ""} Vibes")
            }
            items(state.moodSongs.take(8)) { song ->
                SongRow(
                    song = song,
                    onClick = {
                        viewModel.playSong(song, state.moodSongs, "${state.selectedMood} Vibes")
                        onNavigateToPlayer()
                    },
                )
            }
        }

        // Based on taste
        if (state.basedOnTaste.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Based on Your Taste")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.basedOnTaste) { song ->
                        SongCard(
                            song = song,
                            onClick = {
                                viewModel.playSong(song, state.basedOnTaste, "Based on Your Taste")
                                onNavigateToPlayer()
                            },
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text("See all", color = AccentPrimary)
            }
        }
    }
}

package com.smusic.app.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
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
val moods = listOf("Happy 😊", "Sad 😢", "Party 🎉", "Focus 🎯", "Workout 💪", "Sleep 😴")

val featuredItems = listOf(
    FeaturedItem("Bollywood Hits", "Latest chartbusters from B-town", "🎬", listOf(BollywoodGradient1, BollywoodGradient2, BollywoodGradient3), "Bollywood"),
    FeaturedItem("Chill Vibes", "Relax and unwind with soothing tunes", "🌙", listOf(ChillGradient1, ChillGradient2, ChillGradient3), "Chill"),
    FeaturedItem("Punjabi Beats", "High energy Punjabi bangers", "🔥", listOf(PunjabiGradient1, PunjabiGradient2, PunjabiGradient3), "Punjabi"),
    FeaturedItem("Indie Spotlight", "Discover fresh independent artists", "🎸", listOf(IndieGradient1, IndieGradient2, IndieGradient3), "Indie"),
    FeaturedItem("Romantic Melodies", "Songs that touch the heart", "❤️", listOf(RomanticGradient1, RomanticGradient2, RomanticGradient3), "Romantic"),
    FeaturedItem("Party Anthems", "Get the party started right", "🎉", listOf(PartyGradient1, PartyGradient2, PartyGradient3), "Party"),
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
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Filled.Search, "Search", tint = TextPrimary)
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = TextSecondary)
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Mood chips
        item {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                moods.forEach { mood ->
                    val moodKey = mood.split(" ").first()
                    androidx.compose.material3.FilterChip(
                        selected = state.selectedMood == moodKey,
                        onClick = { viewModel.onMoodSelected(moodKey) },
                        label = { Text(mood, color = TextPrimary, style = MaterialTheme.typography.labelLarge) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = CardElevated,
                            selectedContainerColor = AccentSecondary,
                        ),
                        border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                            borderColor = androidx.compose.ui.graphics.Color.Transparent,
                            selectedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            enabled = true, selected = state.selectedMood == moodKey,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Featured pager
        item {
            SectionHeader("Featured")
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp,
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
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(featuredItems.size) { index ->
                    val color = if (index == pagerState.currentPage) AccentPrimary else TextMuted
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.padding(4.dp).height(6.dp).padding(horizontal = 2.dp),
                    ) {
                        drawCircle(color = color, radius = 3.dp.toPx())
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top Artists
        if (state.topArtists.isNotEmpty()) {
            item {
                SectionHeader("Top Artists")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.topArtists) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { if (artist.artistId.isNotBlank()) onNavigateToArtist(artist.artistId) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Trending Now
        item { SectionHeader("Trending Now") }

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

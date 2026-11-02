package com.smusic.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.ErrorState
import com.smusic.app.presentation.components.ShimmerSongRow
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

data class EditorialCategory(
    val title: String,
    val key: String,
    val backgroundColor: Color,
    val badgeText: String,
)

val editorialCategories = listOf(
    EditorialCategory("Charts", "Trending", Color(0xFF472A52), "Top Songs"),
    EditorialCategory("Tamil Hits", "Tamil", Color(0xFF006450), "Kollywood"),
    EditorialCategory("Telugu Pop", "Telugu", Color(0xFF1E3264), "Tollywood"),
    EditorialCategory("Bollywood", "Bollywood", Color(0xFF8D1627), "Hindi Hits"),
    EditorialCategory("Punjabi", "Punjabi", Color(0xFFBA5D07), "Bhangra"),
    EditorialCategory("Pop", "Pop", Color(0xFF8400E7), "Global"),
    EditorialCategory("Hip-Hop", "Hip-Hop", Color(0xFFBC5900), "Beats"),
    EditorialCategory("Romantic", "Romantic", Color(0xFF900B32), "Melodies"),
    EditorialCategory("Party", "Party", Color(0xFF7D4B32), "Club Mix"),
    EditorialCategory("Workout", "Workout", Color(0xFF0E6E77), "Energy"),
    EditorialCategory("Chill & Focus", "Chill", Color(0xFF283EA3), "Lo-Fi"),
    EditorialCategory("Devotional", "Devotional", Color(0xFF985408), "Spiritual"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onBack: () -> Unit,
    initialCategory: String? = null,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(initialCategory) {
        if (!initialCategory.isNullOrBlank()) {
            viewModel.loadCategory(initialCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding(),
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (initialCategory != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = TextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                BasicTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontFamily = InterFamily,
                    ),
                    cursorBrush = SolidColor(Color(0xFF00E5FF)),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (state.query.isEmpty()) {
                            Text(
                                "What do you want to listen to?",
                                color = TextMuted,
                                fontSize = 15.sp,
                                fontFamily = InterFamily,
                            )
                        }
                        innerTextField()
                    },
                )
                if (state.query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.onQueryChanged("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Filled.Clear, "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Category results
        if (initialCategory != null && state.categorySongs.isNotEmpty()) {
            Text(
                text = state.categoryName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(state.categorySongs, key = { it.videoId }) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.playSong(song, state.categorySongs, state.categoryName)
                            onNavigateToPlayer()
                        },
                    )
                }
            }
            return
        }

        if (state.query.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Recent searches
                if (state.recentSearches.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                        FlowRow(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            state.recentSearches.take(6).forEach { search ->
                                AssistChip(
                                    onClick = {
                                        viewModel.onQueryChanged(search)
                                        viewModel.search(search)
                                    },
                                    label = { Text(search, color = TextPrimary) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.Close,
                                            "Remove",
                                            tint = TextMuted,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeRecentSearch(search) },
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    border = AssistChipDefaults.assistChipBorder(borderColor = Color.White.copy(alpha = 0.12f), enabled = true),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                // Browse all header
                item {
                    Text(
                        text = "Browse all",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }

                // Editorial categories grid
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        editorialCategories.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { category ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(106.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(category.backgroundColor)
                                            .clickable {
                                                viewModel.onQueryChanged(category.key)
                                                viewModel.search(category.key)
                                            }
                                    ) {
                                        // Category title top-left
                                        Text(
                                            text = category.title,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .padding(start = 12.dp, top = 12.dp, end = 40.dp)
                                                .align(Alignment.TopStart)
                                        )

                                        // Apple/Spotify-style angled cover badge pinned in bottom-right corner
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(x = 10.dp, y = 6.dp)
                                                .rotate(20f)
                                                .size(62.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.35f))
                                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Filled.GraphicEq,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = category.badgeText,
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Search results
            when {
                state.isSearching -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(6) { ShimmerSongRow() }
                    }
                }
                state.error != null -> {
                    ErrorState(message = state.error!!, onRetry = { viewModel.search(state.query) })
                }
                state.results.isEmpty() && state.query.length >= 2 -> {
                    EmptyState(message = "No results found for \"${state.query}\"")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(state.results, key = { it.videoId }) { song ->
                            SongRow(
                                song = song,
                                onClick = {
                                    viewModel.playSong(song, state.results, "Search Results")
                                    onNavigateToPlayer()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

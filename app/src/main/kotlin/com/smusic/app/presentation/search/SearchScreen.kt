package com.smusic.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.ErrorState
import com.smusic.app.presentation.components.ShimmerSongRow
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.*

data class CategoryItem(
    val name: String,
    val gradient: List<Color>,
)

val categories = listOf(
    CategoryItem("Trending 🔥", listOf(AccentPrimary, AccentSecondary)),
    CategoryItem("Bollywood 🎬", listOf(BollywoodGradient2, BollywoodGradient3)),
    CategoryItem("Pop 🎵", listOf(ChillGradient2, ChillGradient3)),
    CategoryItem("Hip-Hop 🎤", listOf(PartyGradient2, PartyGradient3)),
    CategoryItem("Rock 🎸", listOf(PunjabiGradient2, PunjabiGradient3)),
    CategoryItem("Tamil 🌟", listOf(IndieGradient2, IndieGradient3)),
    CategoryItem("Telugu 🎭", listOf(RomanticGradient2, RomanticGradient3)),
    CategoryItem("Devotional 🙏", listOf(AccentGold, AccentPrimary)),
    CategoryItem("Romantic ❤️", listOf(RomanticGradient1, RomanticGradient3)),
    CategoryItem("Party 🎉", listOf(PartyGradient1, PartyGradient3)),
    CategoryItem("Workout 💪", listOf(PunjabiGradient1, PunjabiGradient3)),
    CategoryItem("Chill 🌙", listOf(ChillGradient1, ChillGradient3)),
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
            .statusBarsPadding(),
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Search, "Search", tint = TextMuted)
                BasicTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    cursorBrush = SolidColor(AccentPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (state.query.isEmpty()) {
                            Text("Search songs, artists, albums…", color = TextMuted, fontSize = 16.sp)
                        }
                        innerTextField()
                    },
                )
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChanged("") }, modifier = Modifier.height(24.dp)) {
                        Icon(Icons.Filled.Clear, "Clear", tint = TextMuted)
                    }
                }
            }
        }

        // Category results
        if (initialCategory != null && state.categorySongs.isNotEmpty()) {
            Text(
                text = state.categoryName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn {
                items(state.categorySongs) { song ->
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
            // Recent searches
            if (state.recentSearches.isNotEmpty()) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.recentSearches.take(8).forEach { search ->
                        AssistChip(
                            onClick = { viewModel.onQueryChanged(search); viewModel.search(search) },
                            label = { Text(search, color = TextPrimary) },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Close,
                                    "Remove",
                                    tint = TextMuted,
                                    modifier = Modifier.clickable { viewModel.removeRecentSearch(search) },
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = CardElevated),
                            border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent, enabled = true),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Browse categories grid
            Text(
                text = "Browse Categories",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(categories) { category ->
                    val categoryKey = category.name.split(" ").first()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(category.gradient))
                            .clickable { onNavigateToCategory(categoryKey) }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = category.name,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        } else {
            // Search results
            when {
                state.isSearching -> {
                    LazyColumn {
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
                    LazyColumn {
                        items(state.results) { song ->
                            SongRow(
                                song = song,
                                onClick = {
                                    viewModel.playSong(song, state.results, "Search: ${state.query}")
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

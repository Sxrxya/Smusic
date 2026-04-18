package com.smusic.app.presentation.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smusic.app.presentation.components.ErrorState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun ArtistScreen(
    artistId: String,
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(artistId) { viewModel.loadArtist(artistId) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
        }

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    color = AccentPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                )
            }
            state.error != null -> {
                ErrorState(message = state.error!!, onRetry = { viewModel.loadArtist(artistId) })
            }
            else -> {
                val artist = state.artist ?: return
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AsyncImage(
                        model = artist.thumbnailUrl,
                        contentDescription = artist.name,
                        modifier = Modifier.size(120.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(artist.name, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    if (artist.subscriberCount.isNotBlank()) {
                        Text(artist.subscriberCount, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }

                Text(
                    "Top Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                LazyColumn {
                    items(state.songs) { song ->
                        SongRow(
                            song = song,
                            onClick = {
                                viewModel.playSong(song, artist.name)
                                onNavigateToPlayer()
                            },
                        )
                    }
                }
            }
        }
    }
}

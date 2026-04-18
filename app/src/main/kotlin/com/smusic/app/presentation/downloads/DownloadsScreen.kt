package com.smusic.app.presentation.downloads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.components.EmptyState
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.Error
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Downloads", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, modifier = Modifier.weight(1f))
            if (state.songs.isNotEmpty()) {
                IconButton(onClick = { viewModel.deleteAll() }) {
                    Icon(Icons.Filled.DeleteSweep, "Delete All", tint = Error)
                }
            }
        }

        Text(
            text = "Total: ${viewModel.formatSize(state.totalSize)}",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        if (state.songs.isEmpty()) {
            EmptyState("No downloads yet. Download songs for offline listening!")
        } else {
            LazyColumn {
                items(state.songs) { song ->
                    SongRow(
                        song = song,
                        onClick = {
                            viewModel.serviceConnection.playSong(song, state.songs, "Downloads")
                            onNavigateToPlayer()
                        },
                    )
                }
            }
        }
    }
}

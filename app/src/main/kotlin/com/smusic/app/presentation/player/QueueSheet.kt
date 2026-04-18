package com.smusic.app.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smusic.app.domain.model.Song
import com.smusic.app.presentation.components.SongRow
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.BackgroundSurface
import com.smusic.app.presentation.theme.Error
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onPlayIndex: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClearQueue: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BackgroundSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Queue", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                TextButton(onClick = onClearQueue) {
                    Icon(Icons.Filled.DeleteSweep, null, tint = Error, modifier = Modifier.padding(end = 4.dp))
                    Text("Clear", color = Error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (queue.isEmpty()) {
                Text(
                    "Queue is empty",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(32.dp),
                )
            } else {
                LazyColumn {
                    // Now playing
                    if (currentIndex in queue.indices) {
                        item {
                            Text(
                                "Now Playing",
                                color = AccentPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                            SongRow(
                                song = queue[currentIndex],
                                onClick = { },
                                trailing = {
                                    com.smusic.app.presentation.components.EqualizerBars(isPlaying = true)
                                },
                            )
                        }
                    }

                    // Next up
                    val nextUpStart = currentIndex + 1
                    if (nextUpStart < queue.size) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Next Up",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                        itemsIndexed(queue.subList(nextUpStart, queue.size)) { index, song ->
                            val actualIndex = nextUpStart + index
                            SongRow(
                                song = song,
                                onClick = { onPlayIndex(actualIndex) },
                                trailing = {
                                    IconButton(onClick = { onRemove(actualIndex) }) {
                                        Icon(Icons.Filled.Delete, "Remove", tint = TextMuted)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

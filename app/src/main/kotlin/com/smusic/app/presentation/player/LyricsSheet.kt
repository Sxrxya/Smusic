package com.smusic.app.presentation.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.BackgroundSurface
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSheet(
    lyrics: List<LyricLine>,
    isLoading: Boolean,
    currentPositionMs: Long,
    onDismiss: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val listState = rememberLazyListState()

    // Find current lyric index
    val currentIndex = lyrics.indexOfLast { it.timeMs <= currentPositionMs }.coerceAtLeast(0)

    // Auto-scroll to current line
    LaunchedEffect(currentIndex) {
        if (lyrics.isNotEmpty() && currentIndex > 0) {
            listState.animateScrollToItem(
                index = (currentIndex - 2).coerceAtLeast(0),
            )
        }
    }

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
            Text(
                text = "Lyrics",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = AccentPrimary)
                    }
                }
                lyrics.isEmpty() -> {
                    Text(
                        text = "No lyrics available for this song",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    LazyColumn(state = listState) {
                        itemsIndexed(lyrics) { index, line ->
                            val isCurrent = index == currentIndex
                            val isPast = index < currentIndex

                            Text(
                                text = line.text,
                                color = when {
                                    isCurrent -> TextPrimary
                                    isPast -> TextMuted
                                    else -> TextSecondary.copy(alpha = 0.6f)
                                },
                                fontSize = if (isCurrent) 20.sp else 16.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeek(line.timeMs) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

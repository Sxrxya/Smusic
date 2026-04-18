package com.smusic.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.navigation.NavViewModel
import com.smusic.app.presentation.theme.*

@Composable
fun CarModeScreen(
    onExit: () -> Unit,
    navViewModel: NavViewModel = hiltViewModel(),
) {
    val playerState by navViewModel.serviceConnection.playerState.collectAsState()
    val song = playerState.currentSong

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center,
    ) {
        // Exit button
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Close, "Exit Car Mode", tint = TextMuted)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Song info
            Text(
                text = song?.title ?: "No song playing",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song?.artist ?: "",
                color = TextSecondary,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { navViewModel.serviceConnection.previous() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = CardElevated,
                ) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", tint = TextPrimary, modifier = Modifier.padding(20.dp))
                }

                Surface(
                    onClick = { navViewModel.serviceConnection.playPause() },
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    color = AccentPrimary,
                ) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Play/Pause",
                        tint = TextPrimary,
                        modifier = Modifier.padding(24.dp),
                    )
                }

                Surface(
                    onClick = { navViewModel.serviceConnection.next() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = CardElevated,
                ) {
                    Icon(Icons.Filled.SkipNext, "Next", tint = TextPrimary, modifier = Modifier.padding(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Like button
            Surface(
                onClick = { },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = CardBackground,
            ) {
                Icon(Icons.Filled.Favorite, "Like", tint = AccentPrimary, modifier = Modifier.padding(16.dp))
            }

            // Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = CardBackground,
                ) {
                    Icon(Icons.Filled.VolumeDown, "Volume Down", tint = TextPrimary, modifier = Modifier.padding(16.dp))
                }
                Surface(
                    onClick = { },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = CardBackground,
                ) {
                    Icon(Icons.Filled.VolumeUp, "Volume Up", tint = TextPrimary, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

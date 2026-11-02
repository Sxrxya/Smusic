package com.smusic.app.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.R
import com.smusic.app.SMusicApp
import com.smusic.app.presentation.theme.*

@Composable
fun SettingsScreen(
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCarMode: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsState()
    val context = LocalContext.current

    var showStreamQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // Stream Quality Dialog
    if (showStreamQualityDialog) {
        val streamOptions = listOf("320 kbps (High Quality)", "160 kbps (Standard)", "96 kbps (Data Saver)")
        AlertDialog(
            onDismissRequest = { showStreamQualityDialog = false },
            title = { Text("Streaming Quality", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    streamOptions.forEach { option ->
                        val key = option.split(" ").first()
                        val isSelected = prefs.streamQuality.startsWith(key)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateStreamQuality(key)
                                    showStreamQualityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateStreamQuality(key)
                                    showStreamQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFA233B),
                                    unselectedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStreamQualityDialog = false }) {
                    Text("Close", color = Color(0xFFFA233B))
                }
            },
            containerColor = Color(0xFF131722),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Download Quality Dialog
    if (showDownloadQualityDialog) {
        val downloadOptions = listOf("320 kbps (High Quality)", "160 kbps (Standard Quality)")
        AlertDialog(
            onDismissRequest = { showDownloadQualityDialog = false },
            title = { Text("Download Quality", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    downloadOptions.forEach { option ->
                        val key = option.split(" ").first()
                        val isSelected = prefs.downloadQuality.startsWith(key)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateDownloadQuality(key)
                                    showDownloadQualityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateDownloadQuality(key)
                                    showDownloadQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF00E5FF),
                                    unselectedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadQualityDialog = false }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF131722),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Clear Cache Dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Download Cache?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete all offline downloaded songs from your device storage.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDownloads()
                        showClearCacheDialog = false
                        Toast.makeText(context, "All offline downloads cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA233B))
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF131722),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        // Audio Quality
        SectionTitle("Audio Quality")
        SettingsItem("Stream Quality", "${prefs.streamQuality} kbps") {
            showStreamQualityDialog = true
        }
        SettingsItem("Download Quality", "${prefs.downloadQuality} kbps") {
            showDownloadQualityDialog = true
        }
        SettingsClickItem("Equalizer & Sound Effects") { onNavigateToEqualizer() }

        SettingsSliderItem(
            title = "Crossfade",
            value = prefs.crossfadeSeconds.toFloat(),
            range = 0f..12f,
            valueLabel = if (prefs.crossfadeSeconds == 0) "Off" else "${prefs.crossfadeSeconds}s",
            onValueChange = { viewModel.updateCrossfade(it.toInt()) },
        )

        SettingsToggle("Gapless Playback", prefs.gaplessPlayback) { viewModel.updateGapless(it) }
        SettingsToggle("Audio Normalization", prefs.audioNormalization) { viewModel.updateNormalization(it) }
        SettingsToggle("Mono Audio", prefs.monoAudio) { viewModel.updateMonoAudio(it) }

        SettingsDivider()

        // Playback
        SectionTitle("Playback")
        SettingsToggle("Auto-play when queue ends", prefs.autoPlay) { viewModel.updateAutoPlay(it) }
        SettingsClickItem("Car Mode") { onNavigateToCarMode() }
        SettingsToggle("Shake to Skip", prefs.shakeToSkip) { viewModel.updateShakeToSkip(it) }
        SettingsToggle("Pause on headphone unplug", prefs.headphonePause) { viewModel.updateHeadphonePause(it) }
        SettingsToggle("Bluetooth auto-play", prefs.bluetoothAutoPlay) { viewModel.updateBluetoothAutoPlay(it) }

        SettingsDivider()

        // Downloads
        SectionTitle("Downloads")
        SettingsToggle("Download on Wi-Fi only", prefs.downloadWifiOnly) { viewModel.updateDownloadWifiOnly(it) }
        SettingsToggle("Auto-download liked songs", prefs.autoDownloadLiked) { viewModel.updateAutoDownloadLiked(it) }
        SettingsClickItem("Clear Download Cache") { showClearCacheDialog = true }

        SettingsDivider()

        // App
        SectionTitle(stringResource(R.string.app_settings))
        SettingsToggle(stringResource(R.string.startup_animation), prefs.startupAnimationEnabled) { viewModel.updateStartupAnimation(it) }
        SettingsToggle(stringResource(R.string.startup_sound), prefs.startupSoundEnabled) { viewModel.updateStartupSound(it) }
        SettingsToggle(stringResource(R.string.lyrics_lockscreen), prefs.lyricsLockscreen) { viewModel.updateLyricsLockscreen(it) }

        SettingsDivider()

        // About
        SectionTitle(stringResource(R.string.about))
        SettingsItem(
            title = stringResource(R.string.app_version),
            value = "1.0.0",
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        SMusicApp.introShown = false
                    }
                )
            }
        ) { }
        SettingsClickItem("Open Source Licenses") {
            Toast.makeText(context, "SMusic uses ExoPlayer, Room, Ktor, Jetpack Compose", Toast.LENGTH_SHORT).show()
        }
        SettingsClickItem("Privacy Policy") {
            Toast.makeText(context, "SMusic is 100% ad-free & does not collect telemetry.", Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Developed with ❤️ by Surya",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "SMusic v1.0.0 • Hybrid Edition",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF00E5FF),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFA233B),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f),
            ),
        )
    }
}

@Composable
fun SettingsItem(title: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = Color(0xFF00E5FF), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingsClickItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Filled.ChevronRight, null, tint = TextMuted)
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, color = Color(0xFF00E5FF), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF00E5FF),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f),
            ),
        )
    }
}

@Composable
fun SettingsDivider() {
    Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
}

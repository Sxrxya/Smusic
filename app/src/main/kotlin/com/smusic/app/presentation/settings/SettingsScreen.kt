package com.smusic.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.theme.*

@Composable
fun SettingsScreen(
    onNavigateToEqualizer: () -> Unit,
    onNavigateToCarMode: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }

        // Audio Quality
        SectionTitle("Audio Quality")
        SettingsItem("Stream Quality", prefs.streamQuality) { }
        SettingsItem("Download Quality", prefs.downloadQuality) { }
        SettingsClickItem("Equalizer") { onNavigateToEqualizer() }

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
        SettingsToggle("Download on WiFi only", prefs.downloadWifiOnly) { viewModel.updateDownloadWifiOnly(it) }
        SettingsToggle("Auto-download liked songs", prefs.autoDownloadLiked) { viewModel.updateAutoDownloadLiked(it) }

        SettingsDivider()

        // App
        SectionTitle("App")
        SettingsToggle("Show lyrics on lock screen", prefs.lyricsLockscreen) { viewModel.updateLyricsLockscreen(it) }

        SettingsDivider()

        // About
        SectionTitle("About")
        SettingsItem("App Version", "1.0.0") { }
        SettingsClickItem("Open Source Licenses") { }
        SettingsClickItem("Privacy Policy") { }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = AccentPrimary,
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
                checkedThumbColor = TextPrimary,
                checkedTrackColor = AccentPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardElevated,
            ),
        )
    }
}

@Composable
fun SettingsItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
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
            Text(valueLabel, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = AccentPrimary,
                activeTrackColor = AccentPrimary,
                inactiveTrackColor = BorderDivider,
            ),
        )
    }
}

@Composable
fun SettingsDivider() {
    Divider(color = BorderDivider, modifier = Modifier.padding(vertical = 8.dp))
}

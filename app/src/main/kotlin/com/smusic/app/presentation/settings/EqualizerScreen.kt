package com.smusic.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.player.EqualizerManager
import com.smusic.app.presentation.theme.*

val eqPresets = listOf("Flat", "Bass Boost", "Pop", "Rock", "Jazz", "Classical", "Electronic", "Hip-Hop", "R&B", "Vocal")
val eqFrequencyLabels = listOf("32", "64", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsState()
    val bands = remember { mutableStateListOf(*prefs.equalizerBands.toTypedArray()) }
    var bassBoost by remember { mutableIntStateOf(prefs.bassBoost) }
    var virtualizer by remember { mutableIntStateOf(prefs.virtualizer) }
    var loudness by remember { mutableIntStateOf(prefs.loudnessEnhancer) }

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
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary) }
            Text("Equalizer", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                bands.indices.forEach { bands[it] = 0 }
                bassBoost = 0; virtualizer = 0; loudness = 0
            }) { Icon(Icons.Filled.Refresh, "Reset", tint = TextSecondary) }
        }

        // Presets
        LazyRow(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(eqPresets) { preset ->
                val isSelected = prefs.equalizerPreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateStreamQuality(preset) },
                    label = { Text(preset, color = if (isSelected) TextPrimary else TextSecondary) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardBackground,
                        selectedContainerColor = AccentPrimary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = Color.Transparent, selectedBorderColor = Color.Transparent,
                        enabled = true, selected = isSelected,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // EQ bands
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            bands.forEachIndexed { index, level ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("+12", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = level.toFloat(),
                        onValueChange = { bands[index] = it.toInt() },
                        valueRange = -1500f..1500f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentPrimary,
                            activeTrackColor = AccentPrimary,
                            inactiveTrackColor = BorderDivider,
                        ),
                    )
                    Text("-12", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Text(
                        eqFrequencyLabels.getOrElse(index) { "" },
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bass Boost
        SettingsSliderItem(
            title = "Bass Boost",
            value = bassBoost.toFloat(),
            range = 0f..1000f,
            valueLabel = "$bassBoost",
            onValueChange = { bassBoost = it.toInt() },
        )

        // Virtualizer
        SettingsSliderItem(
            title = "Virtualizer",
            value = virtualizer.toFloat(),
            range = 0f..1000f,
            valueLabel = "$virtualizer",
            onValueChange = { virtualizer = it.toInt() },
        )

        // Loudness
        SettingsSliderItem(
            title = "Loudness Enhancer",
            value = loudness.toFloat(),
            range = 0f..1000f,
            valueLabel = "$loudness",
            onValueChange = { loudness = it.toInt() },
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

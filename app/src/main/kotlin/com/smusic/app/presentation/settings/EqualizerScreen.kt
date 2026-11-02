package com.smusic.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smusic.app.presentation.theme.*

val eqPresets = listOf("Flat", "Bass Boost", "Pop", "Rock", "Jazz", "Classical", "Electronic", "Hip-Hop", "R&B", "Vocal")
val eqFrequencyLabels = listOf("32", "64", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.prefs.collectAsState()
    val bands = remember { mutableStateListOf<Int>() }

    LaunchedEffect(prefs.equalizerBands) {
        bands.clear()
        bands.addAll(prefs.equalizerBands)
        if (bands.size < 10) {
            while (bands.size < 10) bands.add(0)
        }
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
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary) }
            Text("Equalizer", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.resetEqualizer() }) {
                Icon(Icons.Filled.Refresh, "Reset", tint = TextSecondary)
            }
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
                    onClick = { viewModel.applyEqualizerPreset(preset) },
                    label = { Text(preset, color = if (isSelected) Color.White else TextSecondary) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = BgCard,
                        selectedContainerColor = Color(0xFFFA233B),
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFFA233B) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
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
                        onValueChangeFinished = { viewModel.updateBandLevel(index, bands[index]) },
                        valueRange = -1500f..1500f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f),
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
            value = prefs.bassBoost.toFloat(),
            range = 0f..1000f,
            valueLabel = "${prefs.bassBoost}",
            onValueChange = { viewModel.updateBassBoost(it.toInt()) },
        )

        // Virtualizer
        SettingsSliderItem(
            title = "Virtualizer",
            value = prefs.virtualizer.toFloat(),
            range = 0f..1000f,
            valueLabel = "${prefs.virtualizer}",
            onValueChange = { viewModel.updateVirtualizer(it.toInt()) },
        )

        // Loudness
        SettingsSliderItem(
            title = "Loudness Enhancer",
            value = prefs.loudnessEnhancer.toFloat(),
            range = 0f..1000f,
            valueLabel = "${prefs.loudnessEnhancer}",
            onValueChange = { viewModel.updateLoudnessEnhancer(it.toInt()) },
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

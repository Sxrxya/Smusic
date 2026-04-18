package com.smusic.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.CardBackground
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

@Composable
fun LanguageChips(
    languages: List<String>,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        languages.forEach { language ->
            val isSelected = language == selectedLanguage
            val containerColor by animateColorAsState(
                if (isSelected) AccentPrimary else CardBackground,
                label = "chip_color",
            )
            val textColor by animateColorAsState(
                if (isSelected) TextPrimary else TextSecondary,
                label = "chip_text",
            )

            FilterChip(
                selected = isSelected,
                onClick = { onLanguageSelected(language) },
                label = {
                    Text(
                        text = language,
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardBackground,
                    selectedContainerColor = AccentPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.Transparent,
                    selectedBorderColor = Color.Transparent,
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

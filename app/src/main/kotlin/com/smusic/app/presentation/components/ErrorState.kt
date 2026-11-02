package com.smusic.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.Error
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary
import com.smusic.app.presentation.theme.TextSecondary

fun formatNetworkError(raw: String?): String {
    if (raw == null) return "No internet connection. Please check your network connection"
    val lower = raw.lowercase()
    if (lower.contains("host") || lower.contains("connect") || lower.contains("timeout") ||
        lower.contains("network") || lower.contains("youtube not reached") ||
        lower.contains("offline") || lower.contains("internet") || lower.contains("ioexception") ||
        lower.contains("socket") || lower.contains("failed to connect") || lower.contains("unresolved")) {
        return "No internet connection. Please check your network connection"
    }
    return raw
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val displayMessage = formatNetworkError(message)
    val isNetworkError = displayMessage == "No internet connection. Please check your network connection"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = if (isNetworkError) Icons.Filled.WifiOff else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = if (isNetworkError) TextSecondary else Error,
            modifier = Modifier.size(52.dp),
        )
        Text(
            text = if (isNetworkError) "You're Offline" else "Something went wrong",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = displayMessage,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = if (isNetworkError) "Try Again" else "Retry",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

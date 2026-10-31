package com.smusic.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smusic.app.presentation.navigation.Screen
import com.smusic.app.presentation.theme.AccentPrimary
import com.smusic.app.presentation.theme.AccentSecondary
import com.smusic.app.presentation.theme.BgSurface
import com.smusic.app.presentation.theme.PrimaryGradient
import com.smusic.app.presentation.theme.TextMuted
import com.smusic.app.presentation.theme.TextPrimary

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val navItems = listOf(
    NavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.Search.route, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    NavItem(Screen.Library.route, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    miniPlayer: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgSurface.copy(alpha = 0.5f))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f), androidx.compose.ui.graphics.Color.Transparent)),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
    ) {
        miniPlayer?.invoke()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val color by animateColorAsState(
                    if (isSelected) AccentPrimary else TextMuted,
                    label = "nav_color",
                )
                val scale by animateFloatAsState(
                    if (isSelected) 1.2f else 1f,
                    label = "nav_scale",
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (isSelected) Modifier.background(PrimaryGradient, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = if (isSelected) TextPrimary else color,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    if (isSelected) {
                        Text(
                            text = item.label,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

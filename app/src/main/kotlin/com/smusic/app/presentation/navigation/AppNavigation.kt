package com.smusic.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.smusic.app.player.SMusicServiceConnection
import com.smusic.app.presentation.album.AlbumScreen
import com.smusic.app.presentation.artist.ArtistScreen
import com.smusic.app.presentation.components.BottomNavBar
import com.smusic.app.presentation.components.MiniPlayer
import com.smusic.app.presentation.downloads.DownloadsScreen
import com.smusic.app.presentation.home.HomeScreen
import com.smusic.app.presentation.library.LibraryScreen
import com.smusic.app.presentation.player.PlayerScreen
import com.smusic.app.presentation.playlist.PlaylistScreen
import com.smusic.app.presentation.search.SearchScreen
import com.smusic.app.presentation.settings.CarModeScreen
import com.smusic.app.presentation.settings.EqualizerScreen
import com.smusic.app.presentation.settings.SettingsScreen
import com.smusic.app.presentation.stats.StatsScreen
import com.smusic.app.presentation.theme.BackgroundPrimary

@Composable
fun AppNavigation(
    navController: NavHostController,
    serviceConnection: SMusicServiceConnection = hiltViewModel<NavViewModel>().serviceConnection,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playerState by serviceConnection.playerState.collectAsState()
    val showMiniPlayer = playerState.currentSong != null &&
            currentRoute != Screen.Player.route &&
            currentRoute != Screen.CarMode.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.Settings.route,
    )

    Scaffold(
        containerColor = BackgroundPrimary,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    miniPlayer = if (showMiniPlayer) {
                        {
                            MiniPlayer(
                                playerState = playerState,
                                onPlayPause = { serviceConnection.playPause() },
                                onNext = { serviceConnection.next() },
                                onClick = { navController.navigate(Screen.Player.route) },
                            )
                        }
                    } else null,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(300)) },
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToArtist = { navController.navigate("artist/$it") },
                        onNavigateToAlbum = { navController.navigate("album/$it") },
                        onNavigateToCategory = { navController.navigate("category/$it") },
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToArtist = { navController.navigate("artist/$it") },
                        onNavigateToAlbum = { navController.navigate("album/$it") },
                        onNavigateToCategory = { navController.navigate("category/$it") },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToPlaylist = { navController.navigate("playlist/$it") },
                        onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                        onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                        onNavigateToCarMode = { navController.navigate(Screen.CarMode.route) },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Screen.Player.route) {
                    PlayerScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToArtist = { navController.navigate("artist/$it") },
                        onNavigateToAlbum = { navController.navigate("album/$it") },
                    )
                }

                composable(Screen.Equalizer.route) {
                    EqualizerScreen(onBack = { navController.popBackStack() })
                }

                composable(Screen.CarMode.route) {
                    CarModeScreen(onExit = { navController.popBackStack() })
                }

                composable(Screen.Downloads.route) {
                    DownloadsScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                    )
                }

                composable(Screen.Stats.route) {
                    StatsScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = Screen.PlaylistDetail.ROUTE,
                    arguments = listOf(navArgument("playlistId") { type = NavType.LongType }),
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    PlaylistScreen(
                        playlistId = playlistId,
                        onBack = { navController.popBackStack() },
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                    )
                }

                composable(
                    route = Screen.ArtistDetail.ROUTE,
                    arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                    ArtistScreen(
                        artistId = artistId,
                        onBack = { navController.popBackStack() },
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToAlbum = { navController.navigate("album/$it") },
                    )
                }

                composable(
                    route = Screen.AlbumDetail.ROUTE,
                    arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                    AlbumScreen(
                        albumId = albumId,
                        onBack = { navController.popBackStack() },
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToArtist = { navController.navigate("artist/$it") },
                    )
                }

                composable(
                    route = Screen.CategoryDetail.ROUTE,
                    arguments = listOf(navArgument("category") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    SearchScreen(
                        initialCategory = category,
                        onNavigateToPlayer = { navController.navigate(Screen.Player.route) },
                        onNavigateToArtist = { navController.navigate("artist/$it") },
                        onNavigateToAlbum = { navController.navigate("album/$it") },
                        onNavigateToCategory = { navController.navigate("category/$it") },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

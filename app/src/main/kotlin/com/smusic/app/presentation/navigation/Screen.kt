package com.smusic.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Settings : Screen("settings")
    data object Player : Screen("player")
    data object Equalizer : Screen("equalizer")
    data object CarMode : Screen("car_mode")
    data object Downloads : Screen("downloads")
    data object Stats : Screen("stats")

    data class PlaylistDetail(val playlistId: Long = 0) : Screen("playlist/{playlistId}") {
        fun createRoute(id: Long) = "playlist/$id"
        companion object { const val ROUTE = "playlist/{playlistId}" }
    }

    data class ArtistDetail(val artistId: String = "") : Screen("artist/{artistId}") {
        fun createRoute(id: String) = "artist/$id"
        companion object { const val ROUTE = "artist/{artistId}" }
    }

    data class AlbumDetail(val albumId: String = "") : Screen("album/{albumId}") {
        fun createRoute(id: String) = "album/$id"
        companion object { const val ROUTE = "album/{albumId}" }
    }

    data class CategoryDetail(val category: String = "") : Screen("category/{category}") {
        fun createRoute(cat: String) = "category/$cat"
        companion object { const val ROUTE = "category/{category}" }
    }
}

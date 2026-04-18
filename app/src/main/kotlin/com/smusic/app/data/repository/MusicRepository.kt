package com.smusic.app.data.repository

import com.smusic.app.data.api.InnerTubeApi
import com.smusic.app.data.api.InnerTubeMapper
import com.smusic.app.data.api.LyricsApi
import com.smusic.app.data.db.dao.RecentlyPlayedDao
import com.smusic.app.data.db.dao.SongDao
import com.smusic.app.data.db.dao.SongStatsDao
import com.smusic.app.data.db.entity.RecentlyPlayedEntity
import com.smusic.app.data.db.entity.SongEntity
import com.smusic.app.data.db.entity.SongStatsEntity
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val innerTubeApi: InnerTubeApi,
    private val lyricsApi: LyricsApi,
    private val songDao: SongDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
    private val songStatsDao: SongStatsDao,
) {

    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.search(query)
            val songs = InnerTubeMapper.mapSearchResponseToSongs(response)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStreamUrl(videoId: String): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getPlayer(videoId)
            val result = InnerTubeMapper.mapPlayerResponseToStreamUrl(response)
            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(Exception("No stream URL available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingSongs(): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getTrending()
            val songs = InnerTubeMapper.mapBrowseResponseToSongs(response)
            if (songs.isNotEmpty()) {
                Result.success(songs)
            } else {
                // Fallback: search for trending Indian music
                val fallback = innerTubeApi.search("trending songs India 2024")
                Result.success(InnerTubeMapper.mapSearchResponseToSongs(fallback))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeSongs(): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getHome()
            val songs = InnerTubeMapper.mapBrowseResponseToSongs(response)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getArtistDetails(artistId: String): Result<Artist> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.browse(artistId)
            val artist = InnerTubeMapper.mapBrowseResponseToArtist(response, artistId)
            Result.success(artist)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAlbumSongs(albumId: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.browse(albumId)
            val songs = InnerTubeMapper.mapBrowseResponseToSongs(response)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLyrics(artist: String, title: String): Result<List<LyricLine>> = withContext(Dispatchers.IO) {
        try {
            val response = lyricsApi.getLyrics(artist, title)
            val lyrics = InnerTubeMapper.mapLrcToLyricLines(response?.syncedLyrics ?: response?.plainLyrics)
            if (lyrics.isNotEmpty()) {
                Result.success(lyrics)
            } else {
                // Fallback search
                val searchResults = lyricsApi.searchLyrics("$artist $title")
                val bestMatch = searchResults.firstOrNull()
                val fallbackLyrics = InnerTubeMapper.mapLrcToLyricLines(
                    bestMatch?.syncedLyrics ?: bestMatch?.plainLyrics
                )
                Result.success(fallbackLyrics)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongsByMood(mood: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val query = when (mood) {
                "Happy" -> "happy upbeat Hindi songs"
                "Sad" -> "sad emotional Hindi songs"
                "Party" -> "party dance Hindi songs"
                "Focus" -> "focus study lo-fi music"
                "Workout" -> "workout gym motivation songs"
                "Sleep" -> "sleep relaxing calm music"
                else -> "$mood songs"
            }
            val response = innerTubeApi.search(query)
            Result.success(InnerTubeMapper.mapSearchResponseToSongs(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongsByLanguage(language: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val query = "latest $language songs 2024"
            val response = innerTubeApi.search(query)
            Result.success(InnerTubeMapper.mapSearchResponseToSongs(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongsByCategory(category: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val query = when (category) {
                "Trending" -> "trending songs India"
                "Bollywood" -> "latest Bollywood songs"
                "Pop" -> "popular pop songs"
                "Hip-Hop" -> "Hindi hip hop rap songs"
                "Rock" -> "rock songs popular"
                "Tamil" -> "latest Tamil songs"
                "Telugu" -> "latest Telugu songs"
                "Devotional" -> "devotional bhajan songs"
                "Romantic" -> "romantic love songs Hindi"
                "Party" -> "party dance songs Hindi"
                "Workout" -> "workout gym songs"
                "Chill" -> "chill relaxing lo-fi"
                else -> "$category songs"
            }
            val response = innerTubeApi.search(query)
            Result.success(InnerTubeMapper.mapSearchResponseToSongs(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordPlay(song: Song) {
        withContext(Dispatchers.IO) {
            // Save to recently played
            recentlyPlayedDao.insertRecentlyPlayed(
                RecentlyPlayedEntity(
                    videoId = song.videoId,
                    title = song.title,
                    artist = song.artist,
                    thumbnailUrl = song.thumbnailUrl,
                    durationSeconds = song.durationSeconds,
                )
            )

            // Update stats
            val existingStats = songStatsDao.getStatsForSong(song.videoId)
            if (existingStats != null) {
                songStatsDao.incrementPlayCount(
                    videoId = song.videoId,
                    listenTimeMs = song.durationSeconds * 1000L,
                    playedAt = System.currentTimeMillis(),
                )
            } else {
                songStatsDao.insertSongStats(
                    SongStatsEntity(
                        videoId = song.videoId,
                        title = song.title,
                        artist = song.artist,
                        thumbnailUrl = song.thumbnailUrl,
                        playCount = 1,
                        totalListenTimeMs = song.durationSeconds * 1000L,
                        lastPlayedAt = System.currentTimeMillis(),
                    )
                )
            }

            // Cache song in DB
            val existing = songDao.getSongById(song.videoId)
            if (existing == null) {
                songDao.insertSong(
                    SongEntity(
                        videoId = song.videoId,
                        title = song.title,
                        artist = song.artist,
                        artistId = song.artistId,
                        album = song.album,
                        albumId = song.albumId,
                        thumbnailUrl = song.thumbnailUrl,
                        durationSeconds = song.durationSeconds,
                        year = song.year,
                    )
                )
            }
        }
    }

    fun getRecentlyPlayed(limit: Int = 30): Flow<List<Song>> {
        return recentlyPlayedDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { e ->
                Song(
                    videoId = e.videoId,
                    title = e.title,
                    artist = e.artist,
                    thumbnailUrl = e.thumbnailUrl,
                    durationSeconds = e.durationSeconds,
                )
            }.distinctBy { it.videoId }
        }
    }

    suspend fun getRecentArtistNames(limit: Int = 5): List<String> = withContext(Dispatchers.IO) {
        recentlyPlayedDao.getRecentArtists(limit)
    }
}

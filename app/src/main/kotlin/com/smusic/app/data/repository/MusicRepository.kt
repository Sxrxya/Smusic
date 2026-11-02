package com.smusic.app.data.repository

import com.smusic.app.data.api.InnerTubeApi
import com.smusic.app.data.api.InnerTubeMapper
import com.smusic.app.data.api.LyricsApi
import com.smusic.app.data.db.dao.RecentlyPlayedDao
import com.smusic.app.data.db.dao.SongDao
import com.smusic.app.data.db.dao.SongStatsDao
import com.smusic.app.data.db.dao.RecommendationDao
import com.smusic.app.data.db.entity.ArtistLanguageEntity
import com.smusic.app.data.db.entity.PlayEventEntity
import com.smusic.app.domain.engine.LanguageResolver
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
    private val recommendationDao: RecommendationDao,
) {

    private val searchCache = mutableMapOf<String, Pair<Long, List<Song>>>()
    private val CACHE_TIMEOUT = 300_000 // 5 minutes

    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        val cached = searchCache[query]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_TIMEOUT) {
            return@withContext Result.success(cached.second)
        }

        try {
            val response = innerTubeApi.search(query)
            val songs = InnerTubeMapper.mapSearchResponseToSongs(response)
            searchCache[query] = System.currentTimeMillis() to songs
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
                val reason = response.playabilityStatus?.reason ?: "No stream URL available"
                Result.failure(Exception(reason))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSongDetails(videoId: String): Result<Song> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getPlayer(videoId)
            val song = InnerTubeMapper.enrichSongWithPlayerResponse(
                Song(videoId = videoId, title = "", artist = ""),
                response
            )
            Result.success(song)
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
            val cleanTitle = title
                .replace(Regex("""(?i)\((from|original|motion picture|soundtrack|feat|ft|audio|video|lyrics?|full song|hd|4k|official)[^)]*\)"""), "")
                .replace(Regex("""(?i)\[[^\]]*\]"""), "")
                .replace(Regex("""(?i)\|.*$"""), "")
                .replace(Regex("""(?i)\b(official|video|audio|lyrics?|full song|hd|4k|lyric video)\b"""), "")
                .trim()
            val cleanArtist = artist.split(",", "&", "feat.", "ft.", "Featuring", "|").first().trim()

            var response = lyricsApi.getLyrics(cleanArtist, cleanTitle)
            if (response == null && cleanTitle != title) {
                response = lyricsApi.getLyrics(cleanArtist, title)
            }
            var lyrics = InnerTubeMapper.mapLrcToLyricLines(response?.syncedLyrics ?: response?.plainLyrics)
            if (lyrics.isNotEmpty()) {
                Result.success(lyrics)
            } else {
                // Fallback search
                val searchResults = lyricsApi.searchLyrics("$cleanTitle $cleanArtist")
                val bestMatch = searchResults.firstOrNull { !it.syncedLyrics.isNullOrBlank() } ?: searchResults.firstOrNull()
                var fallbackLyrics = InnerTubeMapper.mapLrcToLyricLines(
                    bestMatch?.syncedLyrics ?: bestMatch?.plainLyrics
                )
                if (fallbackLyrics.isEmpty()) {
                    // Fallback to JioSaavn Official Lyrics
                    val jioSaavnLyrics = lyricsApi.getJioSaavnLyrics("$cleanTitle $cleanArtist")
                    if (!jioSaavnLyrics.isNullOrBlank()) {
                        fallbackLyrics = InnerTubeMapper.mapLrcToLyricLines(jioSaavnLyrics)
                    }
                }
                Result.success(fallbackLyrics)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getYouTubePlaylist(playlistId: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getPlaylist(playlistId)
            val songs = InnerTubeMapper.mapBrowseResponseToSongs(response)
            if (songs.isNotEmpty()) {
                Result.success(songs)
            } else {
                val searchRes = innerTubeApi.search(playlistId)
                Result.success(InnerTubeMapper.mapSearchResponseToSongs(searchRes))
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

    suspend fun getRelatedSongs(videoId: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = innerTubeApi.getNext(videoId)
            val songs = InnerTubeMapper.mapNextResponseToSongs(response)
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordSongStarted(song: Song) = withContext(Dispatchers.IO) {
        if (song.videoId.isBlank()) return@withContext
        try {
            recentlyPlayedDao.insertRecentlyPlayed(
                RecentlyPlayedEntity(
                    videoId = song.videoId,
                    title = song.title,
                    artist = song.artist,
                    thumbnailUrl = song.thumbnailUrl,
                    durationSeconds = song.durationSeconds,
                )
            )

            val existingStats = songStatsDao.getStatsForSong(song.videoId)
            if (existingStats == null) {
                songStatsDao.insertSongStats(
                    SongStatsEntity(
                        videoId = song.videoId,
                        title = song.title,
                        artist = song.artist,
                        thumbnailUrl = song.thumbnailUrl,
                        playCount = 0,
                        totalListenTimeMs = 0L,
                        lastPlayedAt = System.currentTimeMillis(),
                    )
                )
            }

            // Cache song in DB with genre and language
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
                        language = song.language,
                        genre = song.genre,
                    )
                )
            } else {
                if (existing.genre.isBlank() && song.genre.isNotBlank() ||
                    existing.language.isBlank() && song.language.isNotBlank()
                ) {
                    songDao.updateSong(
                        existing.copy(
                            genre = if (song.genre.isNotBlank()) song.genre else existing.genre,
                            language = if (song.language.isNotBlank()) song.language else existing.language
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore safely
        }
    }

    suspend fun recordListenDuration(song: Song, durationMs: Long) = withContext(Dispatchers.IO) {
        if (song.videoId.isBlank() || durationMs <= 0) return@withContext
        try {
            val existing = songStatsDao.getStatsForSong(song.videoId)
            val now = System.currentTimeMillis()
            if (existing != null) {
                songStatsDao.addListenDuration(song.videoId, durationMs, now)
            } else {
                songStatsDao.insertSongStats(
                    SongStatsEntity(
                        videoId = song.videoId,
                        title = song.title,
                        artist = song.artist,
                        thumbnailUrl = song.thumbnailUrl,
                        playCount = 0,
                        totalListenTimeMs = durationMs,
                        lastPlayedAt = now,
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore safely
        }
    }

    suspend fun recordPlayCount(song: Song) = withContext(Dispatchers.IO) {
        if (song.videoId.isBlank()) return@withContext
        try {
            val existing = songStatsDao.getStatsForSong(song.videoId)
            val now = System.currentTimeMillis()
            if (existing != null) {
                songStatsDao.incrementPlayOnly(song.videoId, now)
            } else {
                songStatsDao.insertSongStats(
                    SongStatsEntity(
                        videoId = song.videoId,
                        title = song.title,
                        artist = song.artist,
                        thumbnailUrl = song.thumbnailUrl,
                        playCount = 1,
                        totalListenTimeMs = 15_000L,
                        lastPlayedAt = now,
                    )
                )
            }

            val lang = song.language
            if (lang.isNotBlank() && song.artistId.isNotBlank()) {
                val artistLang = recommendationDao.getArtistLanguage(song.artistId)
                if (artistLang == null) {
                    recommendationDao.insertArtistLanguage(ArtistLanguageEntity(song.artistId, lang))
                } else {
                    recommendationDao.incrementArtistPlayCount(song.artistId)
                }
            }

            recommendationDao.insertPlayEvent(
                PlayEventEntity(
                    videoId = song.videoId,
                    durationSeconds = song.durationSeconds,
                    listenTimeSeconds = 15,
                    language = song.language,
                    genre = song.genre,
                    artistId = song.artistId
                )
            )
        } catch (e: Exception) {
            // Ignore safely
        }
    }

    suspend fun recordPlay(song: Song) {
        recordSongStarted(song)
    }

    fun getSongsByGenre(genre: String): Flow<List<Song>> {
        return songDao.getSongsByGenre(genre).map { entities ->
            entities.map { it.toSong() }
        }
    }

    fun getSongsByLanguageFlow(language: String): Flow<List<Song>> {
        return songDao.getSongsByLanguage(language).map { entities ->
            entities.map { it.toSong() }
        }
    }

    fun getSongsByGenreAndLanguage(genre: String, language: String): Flow<List<Song>> {
        return songDao.getSongsByGenreAndLanguage(genre, language).map { entities ->
            entities.map { it.toSong() }
        }
    }

    private fun SongEntity.toSong() = Song(
        videoId = videoId,
        title = title,
        artist = artist,
        artistId = artistId,
        album = album,
        albumId = albumId,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        year = year,
        language = language,
        genre = genre,
        isLiked = isLiked,
        streamUrl = streamUrl,
        bitrate = bitrate
    )

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

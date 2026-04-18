package com.smusic.app.data.repository

import com.smusic.app.data.db.dao.AlbumDao
import com.smusic.app.data.db.dao.ArtistDao
import com.smusic.app.data.db.dao.PlaylistDao
import com.smusic.app.data.db.dao.RecentlyPlayedDao
import com.smusic.app.data.db.dao.SongDao
import com.smusic.app.data.db.entity.AlbumEntity
import com.smusic.app.data.db.entity.ArtistEntity
import com.smusic.app.data.db.entity.PlaylistEntity
import com.smusic.app.data.db.entity.PlaylistSongCrossRef
import com.smusic.app.data.db.entity.SongEntity
import com.smusic.app.domain.model.Album
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.Playlist
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val recentlyPlayedDao: RecentlyPlayedDao,
) {

    // ── Liked Songs ────────────────────────────────────
    fun getLikedSongs(): Flow<List<Song>> {
        return songDao.getLikedSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getLikedSongsCount(): Flow<Int> = songDao.getLikedSongsCount()

    suspend fun toggleLike(song: Song): Boolean = withContext(Dispatchers.IO) {
        val existing = songDao.getSongById(song.videoId)
        val newLiked: Boolean
        if (existing != null) {
            newLiked = !existing.isLiked
            songDao.updateLikeStatus(
                videoId = song.videoId,
                liked = newLiked,
                likedAt = if (newLiked) System.currentTimeMillis() else null,
            )
        } else {
            newLiked = true
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
                    isLiked = true,
                    likedAt = System.currentTimeMillis(),
                )
            )
        }
        newLiked
    }

    suspend fun isLiked(videoId: String): Boolean = withContext(Dispatchers.IO) {
        songDao.isLiked(videoId) ?: false
    }

    // ── Playlists ──────────────────────────────────────
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { e ->
                Playlist(
                    playlistId = e.playlistId,
                    name = e.name,
                    thumbnailUrl = e.thumbnailUrl,
                    createdAt = e.createdAt,
                    isPinned = e.isPinned,
                )
            }
        }
    }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        if (playlist != null) {
            playlistDao.deletePlaylist(playlist)
        }
    }

    suspend fun renamePlaylist(playlistId: Long, name: String) = withContext(Dispatchers.IO) {
        playlistDao.renamePlaylist(playlistId, name)
    }

    suspend fun togglePinPlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return@withContext
        playlistDao.updatePinnedStatus(playlistId, !playlist.isPinned)
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) = withContext(Dispatchers.IO) {
        // Ensure song exists in DB
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
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(playlistId = playlistId, videoId = song.videoId)
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, videoId)
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongs(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPlaylistWithSongCount(playlistId: Long): Flow<Pair<Playlist?, Int>> {
        return combine(
            playlistDao.getPlaylistByIdFlow(playlistId),
            playlistDao.getPlaylistSongCount(playlistId),
        ) { playlist, count ->
            val p = playlist?.let {
                Playlist(
                    playlistId = it.playlistId,
                    name = it.name,
                    thumbnailUrl = it.thumbnailUrl,
                    songCount = count,
                    createdAt = it.createdAt,
                    isPinned = it.isPinned,
                )
            }
            Pair(p, count)
        }
    }

    // ── Artists ─────────────────────────────────────────
    fun getFollowedArtists(): Flow<List<Artist>> {
        return artistDao.getFollowedArtists().map { entities ->
            entities.map { e ->
                Artist(
                    artistId = e.artistId,
                    name = e.name,
                    thumbnailUrl = e.thumbnailUrl,
                    subscriberCount = e.subscriberCount,
                    isFollowed = e.isFollowed,
                )
            }
        }
    }

    suspend fun toggleFollowArtist(artist: Artist): Boolean = withContext(Dispatchers.IO) {
        val existing = artistDao.getArtistById(artist.artistId)
        val newFollowed: Boolean
        if (existing != null) {
            newFollowed = !existing.isFollowed
            artistDao.updateFollowStatus(
                artistId = artist.artistId,
                followed = newFollowed,
                followedAt = if (newFollowed) System.currentTimeMillis() else null,
            )
        } else {
            newFollowed = true
            artistDao.insertArtist(
                ArtistEntity(
                    artistId = artist.artistId,
                    name = artist.name,
                    thumbnailUrl = artist.thumbnailUrl,
                    subscriberCount = artist.subscriberCount,
                    isFollowed = true,
                    followedAt = System.currentTimeMillis(),
                )
            )
        }
        newFollowed
    }

    // ── Albums ──────────────────────────────────────────
    fun getSavedAlbums(): Flow<List<Album>> {
        return albumDao.getAllAlbums().map { entities ->
            entities.map { e ->
                Album(
                    albumId = e.albumId,
                    title = e.title,
                    artist = e.artist,
                    artistId = e.artistId,
                    thumbnailUrl = e.thumbnailUrl,
                    year = e.year,
                    songCount = e.songCount,
                )
            }
        }
    }

    suspend fun saveAlbum(album: Album) = withContext(Dispatchers.IO) {
        albumDao.insertAlbum(
            AlbumEntity(
                albumId = album.albumId,
                title = album.title,
                artist = album.artist,
                artistId = album.artistId,
                thumbnailUrl = album.thumbnailUrl,
                year = album.year,
                songCount = album.songCount,
            )
        )
    }

    suspend fun removeAlbum(albumId: String) = withContext(Dispatchers.IO) {
        albumDao.deleteAlbum(albumId)
    }

    // ── History ─────────────────────────────────────────
    fun getHistory(limit: Int = 100): Flow<List<Song>> {
        return recentlyPlayedDao.getRecentlyPlayed(limit).map { entities ->
            entities.map { e ->
                Song(
                    videoId = e.videoId,
                    title = e.title,
                    artist = e.artist,
                    thumbnailUrl = e.thumbnailUrl,
                    durationSeconds = e.durationSeconds,
                )
            }
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        recentlyPlayedDao.clearAll()
    }

    // ── Helpers ─────────────────────────────────────────
    private fun SongEntity.toDomain() = Song(
        videoId = videoId,
        title = title,
        artist = artist,
        artistId = artistId,
        album = album,
        albumId = albumId,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        year = year,
        isLiked = isLiked,
    )
}

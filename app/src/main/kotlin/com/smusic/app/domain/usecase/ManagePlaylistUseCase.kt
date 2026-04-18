package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.LibraryRepository
import com.smusic.app.domain.model.Playlist
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManagePlaylistUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = libraryRepository.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long = libraryRepository.createPlaylist(name)

    suspend fun deletePlaylist(playlistId: Long) = libraryRepository.deletePlaylist(playlistId)

    suspend fun renamePlaylist(playlistId: Long, name: String) = libraryRepository.renamePlaylist(playlistId, name)

    suspend fun togglePin(playlistId: Long) = libraryRepository.togglePinPlaylist(playlistId)

    suspend fun addSong(playlistId: Long, song: Song) = libraryRepository.addSongToPlaylist(playlistId, song)

    suspend fun removeSong(playlistId: Long, videoId: String) = libraryRepository.removeSongFromPlaylist(playlistId, videoId)

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> = libraryRepository.getPlaylistSongs(playlistId)

    fun getPlaylistWithCount(playlistId: Long) = libraryRepository.getPlaylistWithSongCount(playlistId)
}

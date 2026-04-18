package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.LibraryRepository
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLikedSongsUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getLikedSongs()
    }

    fun count(): Flow<Int> = libraryRepository.getLikedSongsCount()
}

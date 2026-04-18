package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.LibraryRepository
import com.smusic.app.domain.model.Song
import javax.inject.Inject

class LikeSongUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
) {
    suspend operator fun invoke(song: Song): Boolean {
        return libraryRepository.toggleLike(song)
    }
}

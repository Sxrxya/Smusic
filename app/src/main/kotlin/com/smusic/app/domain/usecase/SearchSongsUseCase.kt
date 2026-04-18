package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Song
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(query: String): Result<List<Song>> {
        if (query.isBlank()) return Result.success(emptyList())
        return musicRepository.searchSongs(query)
    }
}

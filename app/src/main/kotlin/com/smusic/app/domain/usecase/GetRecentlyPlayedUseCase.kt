package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyPlayedUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    operator fun invoke(limit: Int = 30): Flow<List<Song>> {
        return musicRepository.getRecentlyPlayed(limit)
    }
}

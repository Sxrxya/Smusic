package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Song
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(): Result<List<Song>> {
        return musicRepository.getTrendingSongs()
    }
}

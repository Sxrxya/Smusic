package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.LyricLine
import javax.inject.Inject

class GetLyricsUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(artist: String, title: String): Result<List<LyricLine>> {
        return musicRepository.getLyrics(artist, title)
    }
}

package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.MusicRepository
import javax.inject.Inject

class GetStreamUrlUseCase @Inject constructor(
    private val musicRepository: MusicRepository,
) {
    suspend operator fun invoke(videoId: String): Result<Pair<String, Int>> {
        return musicRepository.getStreamUrl(videoId)
    }
}

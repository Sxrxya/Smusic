package com.smusic.app.domain.usecase

import com.smusic.app.data.api.StreamResolver
import javax.inject.Inject

class GetStreamUrlUseCase @Inject constructor(
    private val streamResolver: StreamResolver
) {
    suspend operator fun invoke(videoId: String): Result<Pair<String, Int>> {
        return streamResolver.resolve(videoId)
    }
}

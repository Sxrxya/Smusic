package com.smusic.app.data.api

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeApi @Inject constructor() {

    private val client = KtorClient.httpClient
    private val baseUrl = "https://music.youtube.com/youtubei/v1"
    private val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-KLET5YdCE"

    private val webRemixContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20230501.01.00",
            hl = "en",
            gl = "IN",
        )
    )

    private val androidMusicContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID_MUSIC",
            clientVersion = "5.28.1",
            hl = "en",
            gl = "IN",
            androidSdkVersion = 30,
        )
    )

    suspend fun search(query: String): SearchResponse {
        return client.post("$baseUrl/search?key=$apiKey") {
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20230501.01.00")
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(
                SearchRequest(
                    context = webRemixContext,
                    query = query,
                )
            )
        }.body()
    }

    suspend fun getPlayer(videoId: String): PlayerResponse {
        return client.post("$baseUrl/player?key=$apiKey") {
            setBody(
                PlayerRequest(
                    context = androidMusicContext,
                    videoId = videoId,
                    playbackContext = PlaybackContext(
                        contentPlaybackContext = ContentPlaybackContext(
                            signatureTimestamp = 19369,
                        )
                    ),
                )
            )
        }.body()
    }

    suspend fun browse(browseId: String): BrowseResponse {
        return client.post("$baseUrl/browse?key=$apiKey") {
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20230501.01.00")
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(
                BrowseRequest(
                    context = webRemixContext,
                    browseId = browseId,
                )
            )
        }.body()
    }

    suspend fun getHome(): BrowseResponse {
        return browse("FEmusic_home")
    }

    suspend fun getTrending(): BrowseResponse {
        return browse("FEmusic_trending")
    }
}

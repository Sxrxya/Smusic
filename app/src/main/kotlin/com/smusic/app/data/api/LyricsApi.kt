package com.smusic.app.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsApi @Inject constructor() {

    private val client = KtorClient.httpClient
    private val baseUrl = "https://lrclib.net/api"

    suspend fun getLyrics(artistName: String, trackName: String): LrcLibResponse? {
        return try {
            client.get("$baseUrl/get") {
                parameter("artist_name", artistName)
                parameter("track_name", trackName)
            }.body<LrcLibResponse>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchLyrics(query: String): List<LrcLibResponse> {
        return try {
            client.get("$baseUrl/search") {
                parameter("q", query)
            }.body<List<LrcLibResponse>>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

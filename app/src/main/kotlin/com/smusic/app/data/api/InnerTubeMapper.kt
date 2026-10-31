package com.smusic.app.data.api



import com.smusic.app.domain.model.Album

import com.smusic.app.domain.model.Artist

import com.smusic.app.domain.model.LyricLine

import com.smusic.app.domain.model.Song



object InnerTubeMapper {



    fun mapSearchResponseToSongs(response: SearchResponse): List<Song> {

        val results = mutableListOf<Song>()

        val sections = response.contents

            ?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()

            ?.tabRenderer?.content?.sectionListRenderer?.contents

            ?: return results



        for (section in sections) {

            val items = section.musicShelfRenderer?.contents ?: continue

            for (item in items) {

                val renderer = item.musicResponsiveListItemRenderer ?: continue

                val song = mapRendererToSong(renderer)

                if (song != null) {

                    results.add(song)

                }

            }

        }

        return results

    }



    private fun mapRendererToSong(renderer: MusicResponsiveListItemRenderer): Song? {

        val columns = renderer.flexColumns ?: return null

        if (columns.size < 2) return null



        val titleRuns = columns[0].musicResponsiveListItemFlexColumnRenderer

            ?.text?.runs ?: return null

        val title = titleRuns.firstOrNull()?.text ?: return null



        val videoId = renderer.playlistItemData?.videoId

            ?: titleRuns.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId

            ?: renderer.overlay?.musicItemThumbnailOverlayRenderer?.content

                ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId

            ?: return null



        val secondColumnRuns = columns[1].musicResponsiveListItemFlexColumnRenderer

            ?.text?.runs ?: emptyList()



        var artist = ""

        var artistId = ""

        var album = ""

        var albumId = ""

        var duration = ""

        var year = ""



        for (run in secondColumnRuns) {

            val text = run.text ?: continue

            if (text == " • " || text == " & " || text == ", ") continue



            val browseEp = run.navigationEndpoint?.browseEndpoint

            val pageType = browseEp?.browseEndpointContextSupportedConfigs

                ?.browseEndpointContextMusicConfig?.pageType



            when {

                pageType == "MUSIC_PAGE_TYPE_ARTIST" -> {

                    artist = text

                    artistId = browseEp?.browseId ?: ""

                }

                pageType == "MUSIC_PAGE_TYPE_ALBUM" -> {

                    album = text

                    albumId = browseEp?.browseId ?: ""

                }

                text.matches(Regex("\\d+:\\d+")) -> {

                    duration = text

                }

                text.matches(Regex("\\d{4}")) -> {

                    year = text

                }

                artist.isEmpty() && browseEp == null -> {

                    // Could be artist without navigation

                    if (!text.matches(Regex("\\d+.*"))) {

                        artist = text

                    }

                }

            }

        }



        val thumbnails = renderer.thumbnail?.musicThumbnailRenderer

            ?.thumbnail?.thumbnails ?: emptyList()

        val thumbnailUrl = thumbnails.maxByOrNull { it.width ?: 0 }?.url ?: ""



        val durationSeconds = parseDuration(duration)



        return Song(

            videoId = videoId,

            title = title,

            artist = artist,

            artistId = artistId,

            album = album,

            albumId = albumId,

            thumbnailUrl = thumbnailUrl,

            durationSeconds = durationSeconds,

            year = year,

        )

    }



    fun mapPlayerResponseToStreamUrl(response: PlayerResponse): Pair<String, Int>? {
        if (response.playabilityStatus?.status != "OK") return null

        val streamingData = response.streamingData ?: return null

        // 1. Prefer direct adaptive audio streams (m4a / opus) with existing URL
        val directAudioFormats = streamingData.adaptiveFormats
            ?.filter { it.mimeType?.contains("audio") == true && !it.url.isNullOrEmpty() }
            ?.sortedByDescending { it.bitrate ?: 0 }

        val bestDirectAudio = directAudioFormats?.firstOrNull()
        if (bestDirectAudio?.url != null) {
            return Pair(bestDirectAudio.url, bestDirectAudio.bitrate ?: 0)
        }

        // 2. Progressive formats (formats array) with direct URLs (e.g. itag 18, itag 22)
        val progressiveFormat = streamingData.formats
            ?.filter { !it.url.isNullOrEmpty() }
            ?.sortedByDescending { it.bitrate ?: 0 }
            ?.firstOrNull()
        if (progressiveFormat?.url != null) {
            return Pair(progressiveFormat.url, progressiveFormat.bitrate ?: 0)
        }

        // 3. Fallback to any direct format in adaptiveFormats
        val anyDirect = streamingData.adaptiveFormats
            ?.filter { !it.url.isNullOrEmpty() }
            ?.maxByOrNull { it.bitrate ?: 0 }
        if (anyDirect?.url != null) {
            return Pair(anyDirect.url, anyDirect.bitrate ?: 0)
        }

        // 4. Fallback to HLS manifest
        streamingData.hlsManifestUrl?.let {
            if (it.isNotEmpty()) return Pair(it, 0)
        }

        return null
    }

    fun enrichSongWithPlayerResponse(song: Song, response: PlayerResponse): Song {

        val details = response.videoDetails ?: return song

        

        return song.copy(

            title = if (song.title.isEmpty()) details.title ?: "" else song.title,

            artist = if (song.artist.isEmpty()) details.author ?: "" else song.artist,

            durationSeconds = if (song.durationSeconds <= 0) details.lengthSeconds?.toIntOrNull() ?: 0 else song.durationSeconds,

            thumbnailUrl = if (song.thumbnailUrl.isEmpty()) {

                details.thumbnail?.thumbnails?.maxByOrNull { it.width ?: 0 }?.url ?: ""

            } else song.thumbnailUrl

        )

    }



    fun mapBrowseResponseToSongs(response: BrowseResponse): List<Song> {

        val results = mutableListOf<Song>()

        val sections = response.contents?.singleColumnBrowseResultsRenderer

            ?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents

            ?: return results



        for (section in sections) {

            val items = section.musicShelfRenderer?.contents ?: continue

            for (item in items) {

                val renderer = item.musicResponsiveListItemRenderer ?: continue

                val song = mapRendererToSong(renderer)

                if (song != null) {

                    results.add(song)

                }

            }

        }

        return results

    }



    fun mapBrowseResponseToArtist(response: BrowseResponse, artistId: String): Artist {

        val header = response.header?.musicImmersiveHeaderRenderer

        val name = header?.title?.runs?.firstOrNull()?.text ?: ""

        val thumbnail = header?.thumbnail?.musicThumbnailRenderer

            ?.thumbnail?.thumbnails?.maxByOrNull { it.width ?: 0 }?.url ?: ""

        val subscriberCount = header?.subscriptionButton

            ?.subscribeButtonRenderer?.subscriberCountText

            ?.runs?.firstOrNull()?.text ?: ""

        val description = header?.description?.runs?.firstOrNull()?.text ?: ""

        val topSongs = mapBrowseResponseToSongs(response)



        return Artist(

            artistId = artistId,

            name = name,

            thumbnailUrl = thumbnail,

            subscriberCount = subscriberCount,

            description = description,

            topSongs = topSongs,

        )

    }



    fun mapNextResponseToSongs(response: NextResponse): List<Song> {

        val results = mutableListOf<Song>()

        val contents = response.contents?.singleColumnMusicWatchNextResultsRenderer

            ?.tabbedRenderer?.watchNextTabbedResultsRenderer?.tabs?.firstOrNull()

            ?.tabRenderer?.content?.musicQueueRenderer?.content?.playlistPanelRenderer?.contents

            ?: return results



        for (content in contents) {

            val renderer = content.playlistPanelVideoRenderer ?: continue

            val videoId = renderer.videoId ?: continue

            val title = renderer.title?.runs?.firstOrNull()?.text ?: continue

            

            val artistRuns = renderer.longBylineText?.runs ?: renderer.shortBylineText?.runs ?: emptyList()

            var artist = ""

            var artistId = ""

            var album = ""

            var albumId = ""



            for (run in artistRuns) {

                val text = run.text ?: continue

                if (text == " • " || text == " & " || text == ", ") continue

                

                val browseId = run.navigationEndpoint?.browseEndpoint?.browseId

                val pageType = run.navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs

                    ?.browseEndpointContextMusicConfig?.pageType



                if (pageType == "MUSIC_PAGE_TYPE_ARTIST") {

                    artist = text

                    artistId = browseId ?: ""

                } else if (pageType == "MUSIC_PAGE_TYPE_ALBUM") {

                    album = text

                    albumId = browseId ?: ""

                } else if (artist.isEmpty()) {

                    artist = text

                }

            }



            val thumbnailUrl = renderer.thumbnail?.thumbnails?.maxByOrNull { it.width ?: 0 }?.url ?: ""

            val durationText = renderer.lengthText?.runs?.firstOrNull()?.text ?: ""

            val durationSeconds = parseDuration(durationText)



            results.add(

                Song(

                    videoId = videoId,

                    title = title,

                    artist = artist,

                    artistId = artistId,

                    album = album,

                    albumId = albumId,

                    thumbnailUrl = thumbnailUrl,

                    durationSeconds = durationSeconds,

                )

            )

        }

        return results

    }



    fun mapLrcToLyricLines(lrcContent: String?): List<LyricLine> {

        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = mutableListOf<LyricLine>()

        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")



        for (line in lrcContent.lines()) {

            val match = regex.find(line) ?: continue

            val (minutes, seconds, hundredths, text) = match.destructured

            val timeMs = minutes.toLong() * 60_000 +

                    seconds.toLong() * 1000 +

                    hundredths.padEnd(3, '0').toLong()

            lines.add(LyricLine(timeMs = timeMs, text = text.trim()))

        }

        return lines.sortedBy { it.timeMs }

    }



    private fun parseDuration(duration: String): Int {

        if (duration.isBlank()) return 0

        val parts = duration.split(":")

        return when (parts.size) {

            2 -> {

                val minutes = parts[0].toIntOrNull() ?: 0

                val seconds = parts[1].toIntOrNull() ?: 0

                minutes * 60 + seconds

            }

            3 -> {

                val hours = parts[0].toIntOrNull() ?: 0

                val minutes = parts[1].toIntOrNull() ?: 0

                val seconds = parts[2].toIntOrNull() ?: 0

                hours * 3600 + minutes * 60 + seconds

            }

            else -> 0

        }

    }

}


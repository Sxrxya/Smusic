package com.smusic.app.domain.usecase

import com.smusic.app.data.db.dao.SongStatsDao
import com.smusic.app.data.db.entity.SongStatsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SongStatsUseCase @Inject constructor(
    private val songStatsDao: SongStatsDao,
) {
    fun getMostPlayed(limit: Int = 10): Flow<List<SongStatsEntity>> =
        songStatsDao.getMostPlayedSongs(limit)

    fun getTotalListeningTime(): Flow<Long?> =
        songStatsDao.getTotalListeningTime()

    suspend fun getMostPlayedSong(): SongStatsEntity? =
        songStatsDao.getMostPlayedSong()

    suspend fun getMostPlayedArtist(): String? {
        val result = songStatsDao.getMostPlayedArtist()
        return result?.artist
    }

    suspend fun getListeningStreak(sinceDaysAgo: Int = 30): Int {
        val sinceMs = System.currentTimeMillis() - (sinceDaysAgo.toLong() * 24 * 60 * 60 * 1000)
        return songStatsDao.getListeningDaysCount(sinceMs)
    }

    fun getTopSongsThisMonth(limit: Int = 10): Flow<List<SongStatsEntity>> {
        val sinceMs = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return songStatsDao.getTopSongsSince(sinceMs, limit)
    }
}

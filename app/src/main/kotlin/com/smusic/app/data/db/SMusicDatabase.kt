package com.smusic.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smusic.app.data.db.dao.AlbumDao
import com.smusic.app.data.db.dao.ArtistDao
import com.smusic.app.data.db.dao.DownloadDao
import com.smusic.app.data.db.dao.PlaylistDao
import com.smusic.app.data.db.dao.RecentlyPlayedDao
import com.smusic.app.data.db.dao.SongDao
import com.smusic.app.data.db.dao.SongStatsDao
import com.smusic.app.data.db.entity.AlbumEntity
import com.smusic.app.data.db.entity.ArtistEntity
import com.smusic.app.data.db.entity.DownloadEntity
import com.smusic.app.data.db.entity.PlaylistEntity
import com.smusic.app.data.db.entity.PlaylistSongCrossRef
import com.smusic.app.data.db.entity.RecentlyPlayedEntity
import com.smusic.app.data.db.entity.SongEntity
import com.smusic.app.data.db.entity.SongStatsEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        ArtistEntity::class,
        AlbumEntity::class,
        RecentlyPlayedEntity::class,
        DownloadEntity::class,
        SongStatsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SMusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun downloadDao(): DownloadDao
    abstract fun songStatsDao(): SongStatsDao
}

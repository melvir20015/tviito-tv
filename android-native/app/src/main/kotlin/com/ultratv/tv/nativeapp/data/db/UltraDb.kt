package com.ultratv.tv.nativeapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE channel ADD COLUMN providerPosition INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE category ADD COLUMN providerPosition INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_channel_providerId_providerPosition ON channel(providerId, providerPosition)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_category_providerId_kind_providerPosition ON category(providerId, kind, providerPosition)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE provider ADD COLUMN epgSourceZoneId TEXT")
        db.execSQL("ALTER TABLE provider ADD COLUMN epgManualOffsetMinutes INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE channel ADD COLUMN epgSourceZoneId TEXT")
        db.execSQL("ALTER TABLE channel ADD COLUMN epgManualOffsetMinutes INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        ProviderEntity::class,
        ChannelEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        CategoryEntity::class,
        FavoriteEntity::class,
        EpgEntity::class,
        WatchHistoryEntity::class,
        RecordingEntity::class,
        com.ultratv.tv.nativeapp.data.reminders.ReminderEntity::class,
    ],
    version = 12,
    exportSchema = true,
)
abstract class UltraDb : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun epgDao(): EpgDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun recordingDao(): RecordingDao
    abstract fun reminderDao(): com.ultratv.tv.nativeapp.data.reminders.ReminderDao
}

package com.elderephemera.podshell.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Feed::class, Episode::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class PodDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun episodesDao(): EpisodeDao

    companion object {
        @Volatile
        private var Instance: PodDatabase? = null

        fun getDatabase(context: Context): PodDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, PodDatabase::class.java, "pod_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
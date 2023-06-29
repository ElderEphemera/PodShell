package com.elderephemera.podshell.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Feed::class],
    version = 1,
    exportSchema = false,
)
abstract class PodDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao

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
package com.elderephemera.podshell.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: Feed)

    @Query("SELECT * FROM feeds ORDER BY id DESC")
    fun getAll(): Flow<List<Feed>>
}
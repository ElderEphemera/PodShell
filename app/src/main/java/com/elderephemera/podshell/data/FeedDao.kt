package com.elderephemera.podshell.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: Feed): Long

    @Query("SELECT * FROM feeds WHERE id = :id")
    suspend fun get(id: Long): Feed

    @Query("SELECT * FROM feeds ORDER BY id DESC")
    fun getAll(): Flow<List<Feed>>

    @Update
    suspend fun update(feed: Feed)

    @Delete
    suspend fun delete(feed: Feed)
}
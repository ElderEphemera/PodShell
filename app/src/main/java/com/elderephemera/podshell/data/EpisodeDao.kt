package com.elderephemera.podshell.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(episode: Episode)

    @Update()
    suspend fun update(episode: Episode)

    @Query("SELECT * FROM episodes WHERE feedId = :feedId")
    fun getAllFromFeed(feedId: Long): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE inPlaylist = TRUE")
    fun getAllInPlaylist(): Flow<List<Episode>>
}
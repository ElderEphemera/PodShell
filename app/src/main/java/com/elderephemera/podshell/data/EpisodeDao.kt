package com.elderephemera.podshell.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(episode: Episode)

    @Query("SELECT * FROM episodes WHERE feedId = :feedId")
    fun getAllFromFeed(feedId: Int): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE inPlaylist = TRUE")
    fun getAllInPlaylist(): Flow<List<Episode>>
}
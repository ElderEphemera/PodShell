package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface EpisodesRepository {
    suspend fun insertEpisode(episode: Episode)
    suspend fun updateEpisode(episode: Episode)
    suspend fun updateEpisodeTime(guid: String, position: Long, length: Long)
    fun getAllFeedEpisodes(feed: Feed): Flow<List<Episode>>
    fun getAllEpisodesInPlaylist(): Flow<List<Episode>>
}
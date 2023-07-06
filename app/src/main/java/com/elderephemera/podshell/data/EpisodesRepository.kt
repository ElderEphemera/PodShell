package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface EpisodesRepository {
    suspend fun insertEpisode(episode: Episode)
    suspend fun updateEpisode(episode: Episode)
    fun getAllFeedEpisodes(feed: Feed): Flow<List<Episode>>
    fun getAllFeedsInPlaylist(): Flow<List<Episode>>
}
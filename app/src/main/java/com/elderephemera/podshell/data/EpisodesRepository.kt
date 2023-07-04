package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface EpisodesRepository {
    suspend fun insertEpisode(episode: Episode)
    fun getAllFeedEpisodes(feed: Feed): Flow<List<Episode>>
}
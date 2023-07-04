package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

class OfflineEpisodesRepository(private val episodeDao: EpisodeDao) : EpisodesRepository {
    override suspend fun insertEpisode(episode: Episode) = episodeDao.insert(episode)
    override fun getAllFeedEpisodes(feed: Feed): Flow<List<Episode>> =
        episodeDao.getAllFromFeed(feed.id)
    override fun getAllFeedsInPlaylist(): Flow<List<Episode>> = episodeDao.getAllInPlaylist()
}
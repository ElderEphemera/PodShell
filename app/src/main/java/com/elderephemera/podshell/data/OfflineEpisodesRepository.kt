package com.elderephemera.podshell.data

class OfflineEpisodesRepository(private val episodeDao: EpisodeDao) : EpisodesRepository {
    override suspend fun insertEpisode(episode: Episode) = episodeDao.insert(episode)
    override suspend fun updateEpisode(episode: Episode) = episodeDao.update(episode)
    override fun getAllFeedEpisodes(feed: Feed) = episodeDao.getAllFromFeed(feed.id)
    override fun getAllEpisodesInPlaylist() = episodeDao.getAllInPlaylist()
}
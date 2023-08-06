package com.elderephemera.podshell.data

class OfflineEpisodesRepository(private val episodeDao: EpisodeDao) : EpisodesRepository {
    override suspend fun insertEpisode(episode: Episode) = episodeDao.insert(episode)

    override suspend fun updateEpisode(episode: Episode) = episodeDao.update(episode)

    override suspend fun updateEpisodeTime(guid: String, position: Long, length: Long) =
        episodeDao.updateTime(guid, position, length)

    override fun getAllFeedEpisodes(feed: Feed) = episodeDao.getAllFromFeed(feed.id)

    override fun getAllEpisodesInPlaylist() = episodeDao.getAllInPlaylist()

    override fun getAllNewEpisodes() = episodeDao.getAllNew()
}
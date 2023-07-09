package com.elderephemera.podshell.data

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.elderephemera.podshell.DownloadsSingleton

class OfflineEpisodesRepository(private val episodeDao: EpisodeDao) : EpisodesRepository {
    override suspend fun insertEpisode(episode: Episode) = episodeDao.insert(episode)

    @OptIn(UnstableApi::class)
    override suspend fun updateEpisode(episode: Episode) {
        episodeDao.update(episode)
        if (!episode.inPlaylist) {
            DownloadsSingleton.getInstance().cacheDataSourceFactory.cache
                ?.removeResource(episode.guid)
        }
    }

    override fun getAllFeedEpisodes(feed: Feed) = episodeDao.getAllFromFeed(feed.id)

    override fun getAllEpisodesInPlaylist() = episodeDao.getAllInPlaylist()
}
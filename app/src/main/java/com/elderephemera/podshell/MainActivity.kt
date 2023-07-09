package com.elderephemera.podshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.ui.AppTab
import com.elderephemera.podshell.ui.NewEpisodesTab
import com.elderephemera.podshell.ui.PlaylistTab
import com.elderephemera.podshell.ui.SubscriptionsTab
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppDataContainer(applicationContext)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    DownloadsSingleton.getInstance(this).cacheDataSourceFactory
                )
            ).build()
        player.addListener(object : Player.Listener {
            private val timer = Timer()
            private var timerTask: TimerTask? = null
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    timerTask = timerTask {
                        CoroutineScope(Dispatchers.Main).launch {
                            player.currentMediaItem?.mediaId?.let {
                                appContainer.episodesRepository
                                    .updateEpisodeTime(it, player.currentPosition, player.duration)
                            }
                        }
                    }
                    timer.scheduleAtFixedRate(timerTask, 0, 1000)
                } else {
                    timerTask?.cancel()
                }
            }
        })

        setContent {
            PodShellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val pagerState = rememberPagerState(0)
                    val tabs = listOf(
                        PlaylistTab(
                            appContainer.feedsRepository,
                            appContainer.episodesRepository,
                            player,
                        ),
                        NewEpisodesTab(),
                        SubscriptionsTab(
                            appContainer.feedsRepository,
                            appContainer.episodesRepository,
                        ),
                    )
                    val animationScope = rememberCoroutineScope()
                    Scaffold(
                        topBar = {
                            TabBar(tabs, pagerState.currentPage) {
                                animationScope.launch {
                                    pagerState.animateScrollToPage(it, 0F)
                                }
                            }
                        },
                    ) { padding ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)) {
                            Pages(tabs, pagerState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabBar(tabs: List<AppTab>, selectedTab: Int, setSelectedTab: (Int) -> Unit) {
    TabRow(selectedTabIndex = selectedTab) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                text = { Text(tab.title) },
                selected = selectedTab == index,
                onClick = { setSelectedTab(index) },
                selectedContentColor = MaterialTheme.colors.onPrimary,
                modifier = Modifier.background(MaterialTheme.colors.primary),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Pages(tabs: List<AppTab>, pagerState: PagerState) {
    HorizontalPager(
        pageCount = 3,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { tabs[it].Content() }
}
package com.elderephemera.podshell

import android.content.ComponentName
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.LegacyPlayerControlView
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.ui.AppTab
import com.elderephemera.podshell.ui.NewEpisodesTab
import com.elderephemera.podshell.ui.PlaylistTab
import com.elderephemera.podshell.ui.SubscriptionsTab
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private var controller: MediaController? by mutableStateOf(null)

    @OptIn(ExperimentalFoundationApi::class)
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppDataContainer(applicationContext)

        val sessionToken =
            SessionToken(this, ComponentName(this, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            { controller = controllerFuture.get() },
            ContextCompat.getMainExecutor(this)
        )

        setContent {
            PodShellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    controller?.let { player ->
                        val pagerState = rememberPagerState(0)
                        val tabs = listOf(
                            PlaylistTab(
                                appContainer.feedsRepository,
                                appContainer.episodesRepository,
                                player,
                            ),
                            NewEpisodesTab(
                                appContainer.feedsRepository,
                                appContainer.episodesRepository,
                            ),
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
                            bottomBar = {
                                AndroidView(factory = {
                                    LegacyPlayerControlView(it).apply {
                                        setPlayer(player)
                                        showTimeoutMs = 0
                                    }
                                })
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

    override fun onDestroy() {
        controller?.release()
        super.onDestroy()
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
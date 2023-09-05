package com.elderephemera.podshell.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import com.elderephemera.podshell.FileManager
import com.elderephemera.podshell.RefreshService
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.prefOverrideTextSize
import com.elderephemera.podshell.prefThemeType
import com.elderephemera.podshell.ui.*
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.launch
import java.util.*

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun Main(
    appContainer: AppDataContainer,
    controller: MediaController?,
    fileManager: FileManager,
    specifiedTab: Int?,
) {
    val context = LocalContext.current

    val overrideTextSize by context.prefOverrideTextSize.state()
    val themeType by context.prefThemeType.state()

    PodShellTheme(
        darkTheme = themeType.isDark,
        overrideTextSize = overrideTextSize,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            controller?.let { player ->
                var fabVisible by remember { mutableStateOf(true) }
                val scrollConnection = remember {
                    object : NestedScrollConnection {
                        val tolerance = 5

                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            if (consumed.y < -tolerance) fabVisible = false
                            else if (consumed.y > tolerance) fabVisible = true
                            return Offset.Zero
                        }
                    }
                }
                val pagerState = rememberPagerState(initialPage = specifiedTab ?: 0)
                val scaffoldState = rememberScaffoldState()
                val tabs = listOf(
                    PlaylistTab(
                        appContainer.feedsRepository,
                        appContainer.episodesRepository,
                        scaffoldState.snackbarHostState,
                        rememberCoroutineScope(),
                        player,
                    ),
                    NewEpisodesTab(
                        appContainer.episodesRepository,
                    ),
                    SubscriptionsTab(
                        appContainer.feedsRepository,
                        appContainer.episodesRepository,
                    ),
                )
                val animationScope = rememberCoroutineScope()
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TabBar(tabs, pagerState.currentPage) {
                            animationScope.launch {
                                pagerState.animateScrollToPage(it, 0F)
                            }
                            if (it == 1) {
                                RefreshService.cancelNotification(context)
                            }
                        }
                    },
                    bottomBar = {
                        PlayerControls(player, fileManager)
                    },
                    floatingActionButton = {
                        Fab(
                            tabs[pagerState.targetPage],
                            fabVisible && !pagerState.isScrollInProgress
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        Pages(tabs, pagerState, scrollConnection)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Fab(tab: AppTab, visible: Boolean) {
    val duration = 150
    val delay = 100
    val easing = FastOutSlowInEasing

    AnimatedContent(
        targetState = Pair(tab, visible),
        transitionSpec = {
            ContentTransform(
                targetContentEnter =
                    expandIn(
                        animationSpec = tween(duration, delay, easing),
                        expandFrom = Alignment.Center,
                        clip = false
                    ) + fadeIn(tween(duration, delay, easing)),
                initialContentExit =
                    shrinkOut(
                        animationSpec = tween(duration, 0, easing),
                        shrinkTowards = Alignment.Center,
                        clip = false
                    ) + fadeOut(tween(duration, 0, easing)),
            )
        },
        contentAlignment = Alignment.Center,
    ) { (currentTab, currentVisibility) ->
        if (currentVisibility) { currentTab.Fab() }
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
fun Pages(tabs: List<AppTab>, pagerState: PagerState, scrollConnection: NestedScrollConnection) {
    HorizontalPager(
        pageCount = 3,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { tabs[it].Content(scrollConnection) }
}
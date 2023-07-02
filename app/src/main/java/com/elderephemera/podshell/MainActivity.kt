package com.elderephemera.podshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.ui.AppTab
import com.elderephemera.podshell.ui.NewEpisodesTab
import com.elderephemera.podshell.ui.PlaylistTab
import com.elderephemera.podshell.ui.SubscriptionsTab
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppDataContainer(applicationContext)
        setContent {
            PodShellTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val pagerState = rememberPagerState(0)
                    val tabs = listOf(
                        PlaylistTab(),
                        NewEpisodesTab(),
                        SubscriptionsTab(appContainer.feedDao),
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
    ) { index ->
        val tab = tabs[index]
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = tab::fabOnClick,
                    backgroundColor = MaterialTheme.colors.primary,
                    content = { tab.FabIcon() },
                )
            },
        ) { padding ->
            tab.AdditionalContent()
            val listItems by tab.listItems().collectAsState(listOf())
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(listItems) {
                    Text(it.title)
                }
            }
        }
    }
}
package com.elderephemera.podshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedDao
import com.elderephemera.podshell.ui.AppTab
import com.elderephemera.podshell.ui.ListItemCard
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.flow.map
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
                        playlistTab(),
                        newEpisodesTab(),
                        subscriptionsTab(appContainer.feedDao),
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
fun playlistTab() = object : AppTab {
    override val title = "PLAYLIST"

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Star, contentDescription = "")
    override fun fabOnClick() {}
}

@Composable
fun newEpisodesTab() = object : AppTab {
    override val title = "NEW EPISODES"

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Star, contentDescription = "")
    override fun fabOnClick() {}
}

fun subscriptionsTab(feedDao: FeedDao) = object : AppTab {
    override val title = "SUBSCRIPTIONS"

    private var showDialog by mutableStateOf(false)

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Add, contentDescription = "Add podcast feed")
    override fun fabOnClick() { showDialog = true }

    override fun listItems() = feedDao.getAll().map { it.map { feed ->
        object : ListItemCard {
            @Composable
            override fun Logo() {}

            override val title = feed.url
            override val url = ""
            override val subtitle = ""
            override val description = ""

            @Composable
            override fun ActionButton() {}

        }
    }}

    @Composable
    override fun AdditionalContent() = AnimatedVisibility(visible = showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val dataScope = rememberCoroutineScope()
                var feedUrl by remember { mutableStateOf("") }
                Text(text = "Add Subscription", fontSize = 20.sp)
                TextField(
                    value = feedUrl,
                    onValueChange = { feedUrl = it },
                    placeholder = { Text(text = "Paste feed URL here") },
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    TextButton(
                        onClick = {
                            dataScope.launch {
                                feedDao.insert(Feed(url = feedUrl))
                                showDialog = false
                            }
                        },
                    ) {
                        Text(text = "SUBSCRIBE")
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
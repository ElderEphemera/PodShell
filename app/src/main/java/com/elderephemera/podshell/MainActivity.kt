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
import androidx.compose.ui.tooling.preview.Preview
import com.elderephemera.podshell.ui.theme.PodShellTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodShellTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val pagerState = rememberPagerState(0)
                    val animationScope = rememberCoroutineScope()
                    Scaffold(
                        topBar = {
                            TabBar(pagerState.currentPage) {
                                animationScope.launch {
                                    pagerState.animateScrollToPage(it, 0F)
                                }
                            }
                        },
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            Pages(pagerState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabBar(selectedTab: Int, setSelectedTab: (Int) -> Unit) {
    TabRow(selectedTabIndex = selectedTab) {
        val tabs = listOf("PLAYLIST", "NEW EPISODES", "SUBSCRIPTIONS")
        tabs.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
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
fun Pages(pagerState: PagerState) {
    HorizontalPager(
        pageCount = 3,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PodShellTheme {
    }
}
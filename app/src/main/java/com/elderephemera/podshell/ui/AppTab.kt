package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AppTab {
    val title : String

    @Composable
    fun FabIcon()
    fun fabOnClick()

    fun listItems(): Flow<List<ListItemCard>> = flowOf(listOf())

    @Composable
    fun Content() = Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = this::fabOnClick,
                backgroundColor = MaterialTheme.colors.primary,
                content = { FabIcon() },
            )
        },
    ) { padding ->
        val listItems by listItems().collectAsState(listOf())
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(10.dp)
        ) {
            items(listItems) {
                it.Content()
            }
        }
    }
}
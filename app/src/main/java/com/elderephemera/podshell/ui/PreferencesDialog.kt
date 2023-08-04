package com.elderephemera.podshell.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderephemera.podshell.prefOverrideTextSize
import kotlinx.coroutines.launch

@Composable
fun preferencesDialog(): () -> Unit {
    var visible by remember { mutableStateOf(false) }

    AnimatedVisibility(visible) {
        Dialog(
            onDismissRequest = { visible = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                TopAppBar(
                    title = { Text("Preferences") },
                    navigationIcon = {
                        IconButton(onClick = { visible = false }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(15.dp)
                ) {
                    Row {
                        val coroutineScope = rememberCoroutineScope()
                        val pref = LocalContext.current.prefOverrideTextSize
                        val value by pref.state()
                        Column(modifier = Modifier.weight(1f, fill = true)) {
                            Text(
                                text = "Override system font size",
                                style = MaterialTheme.typography.subtitle1,
                            )
                            Text(
                                text = "Don't scale text according to the system font size setting",
                                style = MaterialTheme.typography.subtitle2,
                            )
                        }
                        Checkbox(
                            checked = value,
                            onCheckedChange = {
                                coroutineScope.launch { pref.set(it) }
                            },
                            modifier = Modifier.offset(x = 15.dp)
                        )
                    }
                }
            }
        }
    }

    return { visible = true }
}
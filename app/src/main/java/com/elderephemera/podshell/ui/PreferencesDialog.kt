package com.elderephemera.podshell.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderephemera.podshell.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun preferencesDialog(fileManager: FileManager): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                )
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(scrollState)
                ) {
                    Header("Appearance")
                    PrefCheckbox(
                        name = "Override System Font Size",
                        description = "Don't scale text according to the system font size setting",
                        pref = LocalContext.current.prefOverrideTextSize
                    )
                    PrefOptions(
                        name = "Color Theme",
                        pref = LocalContext.current.prefThemeType,
                        options = ThemeType.values(),
                        display = { it.name },
                    )

                    Header("Network")
                    PrefOptions(
                        name = "Auto-refresh Feeds",
                        pref =  LocalContext.current.prefAutoRefreshInterval,
                        options = arrayOf(0, 1, 2, 3, 6, 12, 24, 48, 168, 336),
                        display = {
                            if (it == 0) "Never"
                            else it.hours.toComponents { days, hours, _, _, _ ->
                                val weeksString =
                                    if (days >= 14) "${days/7} weeks"
                                    else if (days >= 7) "1 week"
                                    else null
                                val daysString =
                                    if (days%7 >= 2) "${days%7} days"
                                    else if (days%7 == 1L) "1 day"
                                    else null
                                val hoursString =
                                    if (hours >= 2) "$hours hours"
                                    else if (hours == 1) "1 hour"
                                    else null
                                "Every " + listOfNotNull(weeksString, daysString, hoursString)
                                    .joinToString(" ").removePrefix("1 ")
                            }
                        },
                        onUpdate = { RefreshService.rescheduleRefresh(context) }
                    )

                    Header("Playback")
                    PrefOptions(
                        name = "Jump Forward Interval",
                        pref = LocalContext.current.prefSeekForwardIncrement,
                        options = jumpIntervals,
                        display = ::displayInterval
                    )
                    PrefOptions(
                        name = "Jump Back Interval",
                        pref = LocalContext.current.prefSeekBackIncrement,
                        options = jumpIntervals,
                        display = ::displayInterval
                    )
                    PrefCheckbox(
                        name = "Pause On Headphones Disconnect",
                        description = "Pause playback when switching from a headset or bluetooth device to the internal speakers",
                        pref = LocalContext.current.prefHandleAudioBecomingNoisy
                    )

                    Header("Import/Export")
                    PrefButton(
                        name = "Export To OPML",
                        description = "Export your subscriptions to an OPML file, which is supported by many podcast apps",
                        onClick = { scope.launch { fileManager.exportOpml() } }
                    )
                }
            }
        }
    }

    return { visible = true }
}

@Composable
fun Header(text: String) = Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Divider(color = MaterialTheme.colors.onBackground)
}

@Composable
fun PrefCheckbox(name: String, description: String, pref: Pref<Boolean>) =
    Row(modifier = Modifier.padding(8.dp)) {
        val coroutineScope = rememberCoroutineScope()
        val value by pref.state()

        Column(modifier = Modifier.weight(1f, fill = true)) {
            Text(text = name, style = MaterialTheme.typography.subtitle1)
            Text(text = description, style = MaterialTheme.typography.subtitle2)
        }
        Checkbox(
            checked = value,
            onCheckedChange = {
                coroutineScope.launch { pref.set(it) }
            },
            modifier = Modifier.offset(x = 15.dp)
        )
    }

@Composable
fun <T> PrefOptions(
    name: String,
    pref: Pref<T>,
    options: Array<T>,
    display: (T) -> String,
    onUpdate: () -> Unit = {},
) = Box {
    val coroutineScope = rememberCoroutineScope()
    val value by pref.state()
    var dialogVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(onClick = { dialogVisible = false }) { Text(text = "Cancel") }
            },
            title = { Text(text = name) },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = option == value,
                                    onClick = {
                                        coroutineScope.launch {
                                            pref.set(option)
                                            onUpdate()
                                        }
                                        dialogVisible = false
                                    },
                                    role = Role.RadioButton
                                )

                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            RadioButton(
                                selected = option == value,
                                onClick = null,
                            )
                            Text(
                                text = display(option),
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                    }
                }
            },
        )
    }

    PrefButton(
        name = name,
        description = display(value),
        onClick = { dialogVisible = true }
    )
}

@Composable
fun PrefButton(name: String, description: String, onClick: () -> Unit) = Column(
    modifier = Modifier
        .clickable { onClick() }
        .fillMaxWidth()
        .padding(8.dp)
) {
    Text(text = name, style = MaterialTheme.typography.subtitle1)
    Text(text = description, style = MaterialTheme.typography.subtitle2)
}

private val jumpIntervals = arrayOf<Long>(5_000, 10_000, 20_000, 30_000, 60_000, 300_000)

private fun displayInterval(interval: Long) =
    interval.milliseconds.toComponents { minutes, seconds, _ ->
        if (minutes > 1L) "$minutes minutes"
        else if (minutes == 1L) "1 minute"
        else "$seconds seconds"
    }
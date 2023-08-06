package com.elderephemera.podshell.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlin.time.Duration.Companion.milliseconds

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
                Column(modifier = Modifier.padding(12.dp)) {
                    Header("Appearance")
                    PrefCheckbox(
                        name = "Override system font size",
                        description = "Don't scale text according to the system font size setting",
                        pref = LocalContext.current.prefOverrideTextSize
                    )
                    PrefOptions(
                        name = "Color Theme",
                        pref = LocalContext.current.prefThemeType,
                        options = ThemeType.values(),
                        display = { it.name },
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
fun <T> PrefOptions(name: String, pref: Pref<T>, options: Array<T>, display: (T) -> String) = Box {
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
                                        coroutineScope.launch { pref.set(option) }
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

    Column(
        modifier = Modifier
            .clickable(onClick = { dialogVisible = true })
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle1,
        )
        Text(
            text = display(value),
            style = MaterialTheme.typography.subtitle2,
        )
    }
}

private val jumpIntervals = arrayOf<Long>(5_000, 10_000, 20_000, 30_000, 60_000, 300_000)

private fun displayInterval(interval: Long) =
    interval.milliseconds.toComponents { minutes, seconds, _ ->
        if (minutes > 1L) "$minutes minutes"
        else if (minutes == 1L) "1 minute"
        else "$seconds seconds"
    }
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
import com.elderephemera.podshell.ThemeType
import com.elderephemera.podshell.prefOverrideTextSize
import com.elderephemera.podshell.prefThemeType
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
                Column(modifier = Modifier.padding(8.dp)) {
                    PrefOverrideTextSize()
                    PrefThemeType()
                }
            }
        }
    }

    return { visible = true }
}

@Composable
fun PrefOverrideTextSize() = Row(modifier = Modifier.padding(8.dp)) {
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

@Composable
fun PrefThemeType() = Box {
    val coroutineScope = rememberCoroutineScope()
    val pref = LocalContext.current.prefThemeType
    val value by pref.state()
    var dialogVisible by remember { mutableStateOf(false) }

    AnimatedVisibility(dialogVisible) {
        AlertDialog(
            onDismissRequest = { dialogVisible = false },
            confirmButton = {
                TextButton(onClick = { dialogVisible = false }) { Text(text = "Cancel") }
            },
            title = { Text(text = "Theme Type") },
            text = {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeType.values().forEach { themeType ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = themeType == value,
                                    onClick = {
                                        coroutineScope.launch { pref.set(themeType) }
                                        dialogVisible = false
                                    },
                                    role = Role.RadioButton
                                )

                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            RadioButton(
                                selected = themeType == value,
                                onClick = null,
                            )
                            Text(
                                text = themeType.name,
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
            text = "Color Theme",
            style = MaterialTheme.typography.subtitle1,
        )
        Text(
            text = value.name,
            style = MaterialTheme.typography.subtitle2,
        )
    }
}
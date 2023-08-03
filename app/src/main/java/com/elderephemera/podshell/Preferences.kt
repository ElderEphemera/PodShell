package com.elderephemera.podshell

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.preferencesDataStore by preferencesDataStore(name = "preferences")

class Pref<T>(
    private val context: Context,
    private val key: Preferences.Key<T>,
    private val default: T,
) {
    val flow = context.preferencesDataStore.data.map { it[key] ?: default }

    @Composable
    fun state() = flow.collectAsState(initial = default)

    suspend fun set(value: T) = context.preferencesDataStore.edit { it[key] = value }
}

val Context.prefOverrideTextSize get() = Pref(
    context = this,
    key = booleanPreferencesKey("override-text-size"),
    default = false,
)
package com.elderephemera.podshell

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

val Context.preferencesDataStore by preferencesDataStore(name = "preferences")

interface Pref<T> {
    companion object {
        fun <T, U> inDataStore(
            context: Context,
            key: Preferences.Key<U>,
            default: T,
            encode: (T) -> U,
            decode: (U) -> T,
        ) = DataStorePref(context, key, default, encode, decode)

        fun <T> inDataStore(context: Context, key: Preferences.Key<T>, default: T) =
            DataStorePref(context, key, default, { it }, { it })
    }

    val default: T

    val flow: Flow<T>

    fun stateFlow(scope: CoroutineScope): StateFlow<T> =
        flow.stateIn(scope, SharingStarted.Eagerly, default)

    @Composable
    fun state(): State<T> = flow.collectAsState(initial = default)

    suspend fun set(value: T)
}

class DataStorePref<T, U>(
    private val context: Context,
    private val key: Preferences.Key<U>,
    override val default: T,
    private val encode: (T) -> U,
    private val decode: (U) -> T,
) : Pref<T> {
    override val flow = context.preferencesDataStore.data.map { it[key]?.let(decode) ?: default }

    override suspend fun set(value: T) {
        context.preferencesDataStore.edit { it[key] = encode(value) }
    }
}

val Context.prefOverrideTextSize get() = Pref.inDataStore(
    context = this,
    key = booleanPreferencesKey("override-text-size"),
    default = false,
)

val Context.prefThemeType get() = Pref.inDataStore(
    context = this,
    key = intPreferencesKey("theme-type"),
    default = ThemeType.System,
    encode = { it.ordinal },
    decode = { ThemeType.values()[it] }
)

val Context.prefSeekForwardIncrement get() = Pref.inDataStore(
    context = this,
    key = longPreferencesKey("seek-forward-increment"),
    default = 30_000,
)

val Context.prefSeekBackIncrement get() = Pref.inDataStore(
    context = this,
    key = longPreferencesKey("seek-back-increment"),
    default = 10_000,
)

enum class ThemeType {
    System { override val isDark @Composable get() = isSystemInDarkTheme() },
    Light { override val isDark @Composable get() = false },
    Dark { override val isDark @Composable get() = true };

    @get:Composable
    abstract val isDark: Boolean
}
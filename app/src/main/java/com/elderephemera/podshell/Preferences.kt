package com.elderephemera.podshell

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.preferencesDataStore by preferencesDataStore(name = "preferences")

class Pref<T, U>(
    private val context: Context,
    private val key: Preferences.Key<U>,
    private val default: T,
    private val encode: (T) -> U,
    private val decode: (U) -> T,
) {
    companion object {
        fun <T, U> build(
            context: Context,
            key: Preferences.Key<U>,
            default: T,
            encode: (T) -> U,
            decode: (U) -> T,
        ) = Pref(context, key, default, encode, decode)

        fun <T> build(context: Context, key: Preferences.Key<T>, default: T) =
            Pref(context, key, default, { it }, { it })
    }

    val flow = context.preferencesDataStore.data.map { it[key]?.let(decode) ?: default }

    @Composable
    fun state() = flow.collectAsState(initial = default)

    suspend fun set(value: T) = context.preferencesDataStore.edit { it[key] = encode(value) }
}

val Context.prefOverrideTextSize get() = Pref.build(
    context = this,
    key = booleanPreferencesKey("override-text-size"),
    default = false,
)

val Context.prefThemeType get() = Pref.build(
    context = this,
    key = intPreferencesKey("theme-type"),
    default = ThemeType.System,
    encode = { it.ordinal },
    decode = { ThemeType.values()[it] }
)

enum class ThemeType {
    System { override val isDark @Composable get() = isSystemInDarkTheme() },
    Light { override val isDark @Composable get() = false },
    Dark { override val isDark @Composable get() = true };

    @get:Composable
    abstract val isDark: Boolean
}
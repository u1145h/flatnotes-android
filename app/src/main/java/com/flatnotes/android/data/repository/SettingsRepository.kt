package com.flatnotes.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val PATH_PREFIX = stringPreferencesKey("path_prefix")
        private val SYNC_INTERVAL = longPreferencesKey("sync_interval")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val AMOLED_ENABLED = booleanPreferencesKey("amoled_enabled")
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: ""
    }

    val pathPrefix: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PATH_PREFIX] ?: ""
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[SERVER_URL] = url
        }
    }

    suspend fun savePathPrefix(prefix: String) {
        context.dataStore.edit { prefs ->
            prefs[PATH_PREFIX] = prefix
        }
    }

    val syncInterval: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[SYNC_INTERVAL] ?: 15L
    }

    suspend fun saveSyncInterval(minutes: Long) {
        context.dataStore.edit { prefs ->
            prefs[SYNC_INTERVAL] = minutes
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    val amoledEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AMOLED_ENABLED] ?: false
    }

    suspend fun setAmoledEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AMOLED_ENABLED] = enabled
        }
    }
}

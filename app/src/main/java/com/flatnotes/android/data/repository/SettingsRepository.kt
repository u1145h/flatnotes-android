package com.flatnotes.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val PATH_PREFIX = stringPreferencesKey("path_prefix")
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
}

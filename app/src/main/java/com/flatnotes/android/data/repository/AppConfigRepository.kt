package com.flatnotes.android.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.flatnotes.android.data.api.FlatnotesApiService
import com.flatnotes.android.data.model.AppConfig
import com.flatnotes.android.data.model.NoteCreateDto
import com.flatnotes.android.data.model.NoteUpdateDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.configStore by preferencesDataStore(name = "app_config")

class AppConfigRepository(
    private val api: FlatnotesApiService,
    private val context: Context
) {
    companion object {
        const val CONFIG_NOTE_TITLE = "_flatnotes_config"
        private val CONFIG_CACHE_KEY = stringPreferencesKey("config_cache")
    }

    suspend fun pullFromServer(): AppConfig {
        return try {
            val response = api.getNote(CONFIG_NOTE_TITLE)
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.content ?: "{}"
                AppConfig.fromJson(content)
            } else {
                AppConfig()
            }
        } catch (_: Exception) {
            getLocalConfig()
        }
    }

    suspend fun pushToServer(config: AppConfig) {
        try {
            val content = config.toJson()
            val check = api.getNote(CONFIG_NOTE_TITLE)
            if (check.isSuccessful && check.body() != null) {
                api.updateNote(CONFIG_NOTE_TITLE, NoteUpdateDto(null, content))
            } else {
                api.createNote(NoteCreateDto(CONFIG_NOTE_TITLE, content))
            }
        } catch (_: Exception) {}
    }

    suspend fun saveLocalConfig(config: AppConfig) {
        context.configStore.edit { prefs ->
            prefs[CONFIG_CACHE_KEY] = config.toJson()
        }
    }

    suspend fun getLocalConfig(): AppConfig {
        return context.configStore.data.map { prefs ->
            prefs[CONFIG_CACHE_KEY]?.let { AppConfig.fromJson(it) } ?: AppConfig()
        }.first()
    }
}

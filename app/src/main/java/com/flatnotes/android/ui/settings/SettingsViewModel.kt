package com.flatnotes.android.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.repository.SettingsRepository
import com.flatnotes.android.sync.SyncWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val tokenStorage = TokenStorage(application)

    val syncInterval: StateFlow<Long> = settingsRepository.syncInterval
        .stateIn(viewModelScope, SharingStarted.Eagerly, 15L)

    val themeMode: StateFlow<String> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val amoledEnabled: StateFlow<Boolean> = settingsRepository.amoledEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val serverUrl: StateFlow<String> = settingsRepository.serverUrl
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val pathPrefix: StateFlow<String> = settingsRepository.pathPrefix
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setSyncInterval(minutes: Long) {
        viewModelScope.launch {
            settingsRepository.saveSyncInterval(minutes)
            SyncWorker.reschedule(getApplication(), minutes)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveThemeMode(mode)
        }
    }

    fun setAmoledEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmoledEnabled(enabled)
        }
    }

    fun logout() {
        tokenStorage.clearToken()
    }
}

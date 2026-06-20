package com.flatnotes.android.ui.server

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import com.flatnotes.android.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServerAddressUiState(
    val serverUrl: String = "",
    val pathPrefix: String = "",
    val isLoading: Boolean = false,
    val connectionSuccess: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val authType: String? = null
)

class ServerAddressViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ServerAddressUiState())
    val uiState: StateFlow<ServerAddressUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                serverUrl = tokenStorage.serverUrl,
                pathPrefix = tokenStorage.pathPrefix
            )
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url, error = null, connectionSuccess = false) }
    }

    fun updatePathPrefix(prefix: String) {
        _uiState.update { it.copy(pathPrefix = prefix, connectionSuccess = false) }
    }

    fun testConnection() {
        val url = _uiState.value.serverUrl.trim()
        if (url.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a server URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val prefix = _uiState.value.pathPrefix.trim()
            val api = RetrofitClient.createUnauthenticated(url, prefix)

            try {
                val response = api.healthCheck()
                if (response.isSuccessful) {
                    val config = api.getConfig()
                    val authType = if (config.isSuccessful) config.body()?.authType else "unknown"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            connectionSuccess = true,
                            authType = authType,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Server returned ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Connection failed: ${e.message}")
                }
            }
        }
    }

    fun saveAndExit() {
        val url = _uiState.value.serverUrl.trim()
        val prefix = _uiState.value.pathPrefix.trim()
        val authType = _uiState.value.authType

        viewModelScope.launch {
            tokenStorage.serverUrl = url
            tokenStorage.pathPrefix = prefix
            tokenStorage.clearToken()
            if (authType != null) {
                tokenStorage.authType = authType
            }

            settingsRepository.saveServerUrl(url)
            settingsRepository.savePathPrefix(prefix)

            val db = FlatnotesDatabase.getInstance(getApplication())
            db.noteDao().clearAll()

            _uiState.update { it.copy(saved = true) }
        }
    }
}

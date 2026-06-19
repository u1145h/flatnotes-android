package com.flatnotes.android.ui.server

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.repository.SettingsRepository
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ServerSetupUiState(
    val serverUrl: String = "",
    val pathPrefix: String = "",
    val isLoading: Boolean = false,
    val connectionSuccess: Boolean = false,
    val error: String? = null,
    val authType: String? = null
)

class ServerSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(ServerSetupUiState())
    val uiState: StateFlow<ServerSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.serverUrl.collect { url ->
                _uiState.update { it.copy(serverUrl = url) }
            }
        }
        viewModelScope.launch {
            settingsRepository.pathPrefix.collect { prefix ->
                _uiState.update { it.copy(pathPrefix = prefix) }
            }
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

                    tokenStorage.serverUrl = url
                    tokenStorage.pathPrefix = prefix
                    if (authType != null) {
                        tokenStorage.authType = authType
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

    fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveServerUrl(_uiState.value.serverUrl.trim())
            settingsRepository.savePathPrefix(_uiState.value.pathPrefix.trim())
        }
    }
}

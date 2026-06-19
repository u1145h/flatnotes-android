package com.flatnotes.android.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.repository.AuthRepository
import com.flatnotes.android.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val totpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val authType: String = "password"
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStorage = TokenStorage(application)
    private var authRepository: AuthRepository? = null

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val authType = tokenStorage.authType
        val serverUrl = tokenStorage.serverUrl
        val pathPrefix = tokenStorage.pathPrefix
        _uiState.update { it.copy(authType = authType) }
        if (serverUrl.isNotBlank()) {
            val api = RetrofitClient.create(serverUrl, pathPrefix, tokenStorage)
            authRepository = AuthRepository(api, tokenStorage)
        }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun updateTotpCode(value: String) {
        _uiState.update { it.copy(totpCode = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Username and password are required") }
            return
        }

        val repo = authRepository
        if (repo == null) {
            _uiState.update { it.copy(error = "API not initialized") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val password = if (state.authType == "totp" && state.totpCode.isNotBlank()) {
                "${state.password}${state.totpCode}"
            } else {
                state.password
            }

            when (val result = repo.login(state.username, password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun skipLogin() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }
}

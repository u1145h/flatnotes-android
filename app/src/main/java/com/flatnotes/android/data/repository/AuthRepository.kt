package com.flatnotes.android.data.repository

import com.flatnotes.android.data.api.FlatnotesApiService
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.model.LoginRequestDto
import com.flatnotes.android.data.model.ServerConfigDto
import com.flatnotes.android.util.NetworkResult

class AuthRepository(
    private val api: FlatnotesApiService,
    private val tokenStorage: TokenStorage
) {

    suspend fun getConfig(): NetworkResult<ServerConfigDto> {
        return try {
            val response = api.getConfig()
            if (response.isSuccessful && response.body() != null) {
                tokenStorage.authType = response.body()!!.authType
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error("Failed to get config: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun login(username: String, password: String): NetworkResult<String> {
        return try {
            val response = api.login(LoginRequestDto(username, password))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.accessToken
                tokenStorage.saveToken(token)
                tokenStorage.username = username
                NetworkResult.Success(token)
            } else {
                NetworkResult.Error("Login failed: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    suspend fun checkAuth(): NetworkResult<Boolean> {
        return try {
            val response = api.authCheck()
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Success(false)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Connection error")
        }
    }

    fun logout() {
        tokenStorage.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenStorage.getToken() != null
    }
}

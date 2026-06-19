package com.flatnotes.android.data.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var pathPrefix: String
        get() = prefs.getString(KEY_PATH_PREFIX, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PATH_PREFIX, value).apply()

    var authType: String
        get() = prefs.getString(KEY_AUTH_TYPE, "none") ?: "none"
        set(value) = prefs.edit().putString(KEY_AUTH_TYPE, value).apply()

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "flatnotes_secure_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_PATH_PREFIX = "path_prefix"
        private const val KEY_AUTH_TYPE = "auth_type"
        private const val KEY_USERNAME = "username"
        private const val KEY_TOKEN = "jwt_token"
    }
}

package com.flatnotes.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.repository.SettingsRepository
import com.flatnotes.android.ui.navigation.NavGraph
import com.flatnotes.android.ui.navigation.Routes
import com.flatnotes.android.ui.theme.FlatnotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val tokenStorage = (application as FlatnotesApp).tokenStorage

        setContent {
            val settingsRepo = remember { SettingsRepository(this) }
            val themeMode by settingsRepo.themeMode.collectAsState(initial = "system")
            val amoledEnabled by settingsRepo.amoledEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            val isAmoled = amoledEnabled && darkTheme

            FlatnotesTheme(
                darkTheme = darkTheme,
                isAmoled = isAmoled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val startDestination = remember {
                        determineStartDestination(tokenStorage)
                    }

                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    private fun determineStartDestination(tokenStorage: TokenStorage): String {
        val serverUrl = tokenStorage.serverUrl
        val token = tokenStorage.getToken()
        val authType = tokenStorage.authType

        return when {
            serverUrl.isBlank() -> Routes.SERVER_SETUP
            token != null -> Routes.NOTE_LIST
            authType == "none" || authType == "read_only" -> Routes.NOTE_LIST
            else -> Routes.LOGIN
        }
    }
}

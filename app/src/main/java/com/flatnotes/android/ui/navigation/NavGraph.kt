package com.flatnotes.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.flatnotes.android.ui.login.LoginScreen
import com.flatnotes.android.ui.login.LoginViewModel
import com.flatnotes.android.ui.notes.NoteEditorScreen
import com.flatnotes.android.ui.notes.NoteEditorViewModel
import com.flatnotes.android.ui.notes.NoteListScreen
import com.flatnotes.android.ui.notes.NoteListViewModel
import com.flatnotes.android.ui.server.ServerSetupScreen
import com.flatnotes.android.ui.server.ServerSetupViewModel
import com.flatnotes.android.ui.settings.SettingsScreen
import com.flatnotes.android.ui.settings.SettingsViewModel
import com.flatnotes.android.ui.settings.SyncSettingsScreen
import com.flatnotes.android.ui.settings.ThemeSettingsScreen

object Routes {
    const val SERVER_SETUP = "server_setup"
    const val LOGIN = "login"
    const val NOTE_LIST = "note_list"
    const val NOTE_EDITOR = "note_editor/{title}"
    const val NOTE_CREATE = "note_create"
    const val SETTINGS = "settings"
    const val SYNC_SETTINGS = "sync_settings"
    const val THEME_SETTINGS = "theme_settings"

    fun noteEditor(title: String) = "note_editor/$title"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SERVER_SETUP) {
            val viewModel: ServerSetupViewModel = viewModel()
            ServerSetupScreen(
                viewModel = viewModel,
                onConnected = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SERVER_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel()
            // ViewModel has already been initialized in Application context
            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = {
                    navController.navigate(Routes.NOTE_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTE_LIST) {
            val viewModel: NoteListViewModel = viewModel()
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { title ->
                    navController.navigate(Routes.noteEditor(title))
                },
                onCreateNote = {
                    navController.navigate(Routes.NOTE_CREATE)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.NOTE_LIST) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.NOTE_EDITOR,
            arguments = listOf(navArgument("title") { type = NavType.StringType })
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val viewModel: NoteEditorViewModel = viewModel()
            NoteEditorScreen(
                viewModel = viewModel,
                title = title,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTE_CREATE) {
            val viewModel: NoteEditorViewModel = viewModel()
            NoteEditorScreen(
                viewModel = viewModel,
                title = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSync = { navController.navigate(Routes.SYNC_SETTINGS) },
                onNavigateToTheme = { navController.navigate(Routes.THEME_SETTINGS) }
            )
        }

        composable(Routes.SYNC_SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SyncSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.THEME_SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            ThemeSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

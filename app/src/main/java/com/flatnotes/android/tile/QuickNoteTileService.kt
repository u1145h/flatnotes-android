package com.flatnotes.android.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.flatnotes.android.MainActivity
import com.flatnotes.android.R
import com.flatnotes.android.data.api.TokenStorage

class QuickNoteTileService : TileService() {

    companion object {
        private const val TAG = "QuickNoteTile"
    }

    private var tokenStorage: TokenStorage? = null

    override fun onCreate() {
        super.onCreate()
        try {
            tokenStorage = TokenStorage(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create TokenStorage", e)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        val ts = tokenStorage
        if (ts == null) {
            Log.w(TAG, "TokenStorage not available, opening MainActivity")
            openMainActivity()
            return
        }

        val serverUrl = ts.serverUrl
        val token = ts.getToken()
        val authType = ts.authType

        if (serverUrl.isBlank()) {
            Log.d(TAG, "No server configured, opening MainActivity")
            openMainActivity()
            return
        }

        val isAuthenticated = token != null || authType == "none" || authType == "read_only"

        if (!isAuthenticated) {
            Log.d(TAG, "Not authenticated, opening MainActivity")
            openMainActivity()
            return
        }

        Log.d(TAG, "Starting QuickNoteCreationActivity")
        startQuickNoteActivity()
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    private fun startQuickNoteActivity() {
        val intent = Intent(this, QuickNoteCreationActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            )
        }
        if (Build.VERSION.SDK_INT >= 31) {
            val pendingIntent = PendingIntent.getActivity(
                this, 1, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val ts = tokenStorage

        val isReady = ts != null && ts.serverUrl.isNotBlank() &&
            (ts.getToken() != null || ts.authType == "none" || ts.authType == "read_only")

        tile.state = if (isReady) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.quick_note_tile_label)
        try {
            tile.icon = Icon.createWithResource(this, R.drawable.ic_quick_note)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set tile icon", e)
        }
        tile.updateTile()
    }
}

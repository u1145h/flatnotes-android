package com.flatnotes.android

import android.app.Application
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.repository.SettingsRepository
import com.flatnotes.android.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlatnotesApp : Application() {

    lateinit var tokenStorage: TokenStorage
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        tokenStorage = TokenStorage(this)

        applicationScope.launch {
            val interval = SettingsRepository(this@FlatnotesApp).syncInterval.first()
            SyncWorker.enqueue(this@FlatnotesApp, interval)
        }
    }
}

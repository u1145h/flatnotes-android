package com.flatnotes.android

import android.app.Application
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.sync.SyncWorker

class FlatnotesApp : Application() {

    lateinit var tokenStorage: TokenStorage
        private set

    override fun onCreate() {
        super.onCreate()
        tokenStorage = TokenStorage(this)
        SyncWorker.enqueue(this)
    }
}

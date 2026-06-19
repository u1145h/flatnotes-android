package com.flatnotes.android.sync

import android.content.Context
import androidx.work.*
import com.flatnotes.android.data.api.RetrofitClient
import com.flatnotes.android.data.api.TokenStorage
import com.flatnotes.android.data.local.FlatnotesDatabase
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val tokenStorage = TokenStorage(applicationContext)
        val serverUrl = tokenStorage.serverUrl
        val pathPrefix = tokenStorage.pathPrefix
        val token = tokenStorage.getToken()

        if (serverUrl.isBlank() || token == null) {
            return Result.success()
        }

        val api = RetrofitClient.create(serverUrl, pathPrefix, tokenStorage)
        val dao = FlatnotesDatabase.getInstance(applicationContext).noteDao()
        val syncManager = SyncManager(api, dao)

        val result = syncManager.sync()

        return if (result.errors.isEmpty()) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "flatnotes_sync"

        fun enqueue(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(intervalMinutes, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }

        fun reschedule(context: Context, intervalMinutes: Long) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            enqueue(context, intervalMinutes)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

package com.github.fjbaldon.attendex.capture.data.sync

import android.content.Context
import androidx.work.*
import com.github.fjbaldon.attendex.capture.core.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Call this when the app starts (e.g. in MainActivity or Application onCreate)
     * It ensures a background job runs every 15 minutes forever.
     */
    fun startPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only run if we have internet
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "AttendEx_Periodic_Sync",
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            request
        )
    }

    /**
     * Call this immediately after a successful scan in ScannerViewModel.
     * It attempts to upload immediately ("One-time shot").
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "AttendEx_Immediate_Sync",
            ExistingWorkPolicy.APPEND, // Changed from APPEND_OR_REPLACE to prevent cancelling active uploads
            request
        )
    }
}

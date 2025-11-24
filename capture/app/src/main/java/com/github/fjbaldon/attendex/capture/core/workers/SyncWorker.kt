package com.github.fjbaldon.attendex.capture.core.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventRepository: EventRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Optimization: Don't spin up network if DB is empty
        val pendingCount = eventRepository.getUnsyncedCount()
        if (pendingCount == 0) {
            Log.i("SyncWorker", "No unsynced entries found. Worker sleeping.")
            return Result.success()
        }

        Log.i("SyncWorker", "Found $pendingCount items. Starting sync...")

        return try {
            // 2. Run the sync
            val result = eventRepository.syncEntries()

            if (result.isSuccess) {
                Log.i("SyncWorker", "Sync successful.")
                Result.success()
            } else {
                Log.w("SyncWorker", "Sync failed. Retrying later...")
                // 3. CRITICAL: Return retry() so WorkManager uses exponential backoff
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Exception during sync", e)
            Result.retry()
        }
    }
}

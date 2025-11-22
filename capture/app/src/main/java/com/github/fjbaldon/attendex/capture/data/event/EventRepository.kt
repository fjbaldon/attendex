package com.github.fjbaldon.attendex.capture.data.event

import android.util.Log
import androidx.room.withTransaction
import com.github.fjbaldon.attendex.capture.core.data.local.AppDatabase
import com.github.fjbaldon.attendex.capture.core.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EntryDao
import com.github.fjbaldon.attendex.capture.core.data.local.dao.EventDao
import com.github.fjbaldon.attendex.capture.core.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.core.data.local.model.EntryEntity
import com.github.fjbaldon.attendex.capture.core.data.local.model.EventEntity
import com.github.fjbaldon.attendex.capture.core.data.local.model.SyncStatus
import com.github.fjbaldon.attendex.capture.core.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.core.data.remote.EntrySyncRequest
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannedItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.Instant
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase,
    private val eventDao: EventDao,
    private val attendeeDao: AttendeeDao,
    private val entryDao: EntryDao
) {
    private var lastScannedIdentity: String? = null
    private val syncMutex = Mutex()

    val hasUnsyncedRecords: Flow<Boolean> = entryDao.getUnsyncedEntryCount()
        .map { count -> count > 0 }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventNameById(eventId: Long): String? = eventDao.getEventById(eventId)?.name

    suspend fun isEventActive(eventId: Long): Boolean {
        val event = eventDao.getEventById(eventId) ?: return false
        val now = Instant.now()
        return try {
            val start = Instant.parse(event.startDate)
            val end = Instant.parse(event.endDate)
            now.isAfter(start.minusSeconds(3600)) && now.isBefore(end.plusSeconds(3600))
        } catch (_: Exception) {
            true
        }
    }

    suspend fun refreshEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteEvents = apiService.getActiveEvents()
            val eventEntities = remoteEvents.map {
                EventEntity(it.id, it.name, it.startDate, it.endDate, it.sessions)
            }

            // 1. Transaction: Clear old data and insert Events
            appDatabase.withTransaction {
                eventDao.clearAll()
                eventDao.insertAll(eventEntities)
                attendeeDao.clearAll() // Wipe all attendees to start fresh
            }

            // 2. Batch Fetch & Insert Attendees (Event by Event)
            // We do NOT wrap this entire loop in a transaction to avoid holding the DB lock
            // while waiting for the Network.
            remoteEvents.forEach { event ->
                var page = 0
                var isLast = false
                while (!isLast) {
                    // Fetch one page
                    val res = apiService.getAttendeesForEvent(event.id, page, 500)

                    val batch = res.content.map {
                        AttendeeEntity(
                            localId = 0,
                            eventId = event.id,
                            attendeeId = it.attendeeId,
                            identity = it.identity,
                            firstName = it.firstName,
                            lastName = it.lastName
                        )
                    }

                    // Insert immediately
                    if (batch.isNotEmpty()) {
                        attendeeDao.insertAll(batch)
                    }

                    isLast = res.last
                    page++
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Refresh failed", e)
            Result.failure(e)
        }
    }

    suspend fun primeLastScannedIdentifier(eventId: Long) {
        val lastEntry = entryDao.findMostRecentEntryForEvent(eventId)
        lastScannedIdentity = lastEntry?.snapshotIdentity
    }

    suspend fun processScan(eventId: Long, identity: String): ScanResult {
        if (identity == lastScannedIdentity) return ScanResult.AlreadyScanned

        val attendee = attendeeDao.findAttendeeByIdentity(eventId, identity)
            ?: return ScanResult.AttendeeNotFound

        val entry = EntryEntity(
            eventId = eventId,
            attendeeId = attendee.attendeeId,
            snapshotIdentity = attendee.identity,
            snapshotFirstName = attendee.firstName,
            snapshotLastName = attendee.lastName,
            scanTimestamp = Instant.now(),
            syncStatus = SyncStatus.PENDING
        )

        entryDao.insert(entry)
        lastScannedIdentity = identity

        return ScanResult.Success(attendee, "Scanned")
    }

    suspend fun syncEntries(): Result<Int> = withContext(Dispatchers.IO) {
        if (syncMutex.isLocked) return@withContext Result.success(0)

        syncMutex.withLock {
            try {
                val batch = entryDao.getPendingEntriesBatch(50)
                if (batch.isEmpty()) return@withLock Result.success(0)

                val request = EntrySyncRequest(
                    records = batch.map {
                        EntrySyncRequest.EntryRecord(
                            it.scanUuid,
                            it.eventId,
                            it.attendeeId,
                            it.scanTimestamp.toString()
                        )
                    }
                )

                val response = apiService.syncEntries(request)

                val allUuids = batch.map { it.scanUuid }
                val failedUuids = response.failedUuids.toSet()
                val successUuids = allUuids.filter { !failedUuids.contains(it) }

                if (successUuids.isNotEmpty()) {
                    entryDao.markAsSyncedByUuid(successUuids)
                }

                if (failedUuids.isNotEmpty()) {
                    entryDao.markAsFailedByUuid(failedUuids.toList())
                }

                if (batch.size == 50 && failedUuids.isEmpty()) {
                    syncEntries()
                }

                Result.success(response.successCount)
            } catch (e: Exception) {
                // POISON PILL FIX
                if (e is HttpException && e.code() in 400..499) {
                    // Client Error (e.g. 400 Bad Request).
                    // This data will never be accepted. Mark as FAILED.
                    val batchUuids = entryDao.getPendingEntriesBatch(50).map { it.scanUuid }
                    entryDao.markAsFailedByUuid(batchUuids)
                    Result.failure(Exception("Sync Rejected by Server (Code ${e.code()}). Marked as Failed."))
                } else {
                    // Network/Server Error (500). Retry later.
                    Result.failure(e)
                }
            }
        }
    }

    fun getScannedItemsStream(eventId: Long): Flow<List<ScannedItemUi>> {
        return entryDao.getEntriesForEventStream(eventId).map { entries ->
            entries.map { entry ->
                ScannedItemUi(
                    id = entry.id.toLong(),
                    name = "${entry.snapshotLastName}, ${entry.snapshotFirstName}",
                    identity = entry.snapshotIdentity,
                    isSynced = entry.syncStatus == SyncStatus.SYNCED,
                    isFailed = entry.syncStatus == SyncStatus.FAILED
                )
            }
        }
    }

    suspend fun getUnsyncedCount(): Int = entryDao.getUnsyncedCountSnapshot()

    suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        appDatabase.withTransaction {
            entryDao.clearAll()
            attendeeDao.clearAll()
            eventDao.clearAll()
        }
    }
}

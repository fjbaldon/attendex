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
import com.github.fjbaldon.attendex.capture.core.data.remote.EventStatsResponse
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannedItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.Instant
import java.time.temporal.ChronoUnit
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

    fun searchAttendees(eventId: Long, query: String): Flow<List<AttendeeEntity>> {
        return attendeeDao.searchAttendees(eventId, query)
    }

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

            appDatabase.withTransaction {
                if (eventEntities.isNotEmpty()) {
                    eventDao.insertAll(eventEntities)
                    val activeIds = eventEntities.map { it.id }
                    eventDao.deleteEventsNotIn(activeIds)
                } else {
                    eventDao.clearAll()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Refresh failed", e)
            Result.failure(e)
        }
    }

    suspend fun syncRosterForEvent(eventId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // 1. Clear existing roster first (Transaction safe)
            attendeeDao.clearAttendeesForEvent(eventId)

            var page = 0
            var isLast = false
            var totalSynced = 0

            while (!isLast) {
                // Fetch Batch
                val res = apiService.getAttendeesForEvent(eventId, page, 500)

                // Map to Entity
                val batchEntities = res.content.map {
                    AttendeeEntity(
                        localId = 0,
                        eventId = eventId,
                        attendeeId = it.attendeeId,
                        identity = it.identity,
                        firstName = it.firstName,
                        lastName = it.lastName
                    )
                }

                // 2. Insert immediately
                if (batchEntities.isNotEmpty()) {
                    attendeeDao.insertAll(batchEntities)
                }

                totalSynced += batchEntities.size
                isLast = res.last
                page++
            }

            Result.success(totalSynced)
        } catch (e: Exception) {
            Log.e("EventRepository", "Roster sync failed for $eventId", e)
            Result.failure(e)
        }
    }

    suspend fun retryFailedEntries(eventId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            entryDao.resetFailedToPendingForEvent(eventId)
            syncEntries()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchEventStats(eventId: Long): Result<EventStatsResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(apiService.getEventStats(eventId))
        } catch (e: Exception) {
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
                            it.scanTimestamp.toString(),
                            it.snapshotIdentity,
                            it.snapshotFirstName,
                            it.snapshotLastName
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
                if (e is HttpException && e.code() in 400..499) {
                    val batchUuids = entryDao.getPendingEntriesBatch(50).map { it.scanUuid }
                    entryDao.markAsFailedByUuid(batchUuids)
                    Result.failure(Exception("Sync Rejected by Server (Code ${e.code()}). Marked as Failed."))
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun runHousekeeping() = withContext(Dispatchers.IO) {
        try {
            // Delete entries older than 24 hours
            val threshold = Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli()
            val count = entryDao.deleteSyncedEntriesOlderThan(threshold)
            if (count > 0) {
                Log.i("EventRepository", "Housekeeping: Deleted $count old synced entries.")
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Housekeeping failed", e)
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

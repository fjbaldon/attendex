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
import com.github.fjbaldon.attendex.capture.core.data.remote.RosterItemResponse
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannedItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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

    fun getUnsyncedCountFlow(): Flow<Int> = entryDao.getUnsyncedEntryCount()

    val hasUnsyncedRecords: Flow<Boolean> = entryDao.getUnsyncedEntryCount()
        .map { count -> count > 0 }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventNameById(eventId: Long): String? = eventDao.getEventById(eventId)?.name

    fun searchAttendees(eventId: Long, query: String): Flow<List<AttendeeEntity>> {
        return attendeeDao.searchAttendees(eventId, query)
    }

    suspend fun getLocalEventStats(eventId: Long): Pair<Long, Long> {
        val rosterCount = attendeeDao.countAttendeesForEvent(eventId)
        val scanCount = entryDao.countEntriesForEvent(eventId)
        return Pair(scanCount, rosterCount)
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

            // FIX: Eagerly sync rosters for all active events
            // This ensures that if the user goes offline immediately after this screen,
            // the rosters are populated.
            eventEntities.forEach { event ->
                try {
                    syncRosterForEvent(event.id)
                } catch (e: Exception) {
                    Log.w("EventRepository", "Failed to pre-fetch roster for ${event.name}", e)
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
            // 1. Fetch First Page
            val firstPageResponse = try {
                apiService.getAttendeesForEvent(eventId, 0, 500)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }

            // 2. Accumulate all data in memory
            val allAttendees = ArrayList<AttendeeEntity>()
            allAttendees.addAll(firstPageResponse.content.map { mapToAttendeeEntity(eventId, it) })

            var isLast = firstPageResponse.last
            var page = 1

            while (!isLast) {
                // FIXED: 'isActive' replaced with 'ensureActive()'
                currentCoroutineContext().ensureActive()

                val res = apiService.getAttendeesForEvent(eventId, page, 500)
                allAttendees.addAll(res.content.map { mapToAttendeeEntity(eventId, it) })

                isLast = res.last
                page++
            }

            // 3. Atomic DB Update
            appDatabase.withTransaction {
                attendeeDao.clearAttendeesForEvent(eventId)
                if (allAttendees.isNotEmpty()) {
                    attendeeDao.insertAll(allAttendees)
                }
            }

            Result.success(allAttendees.size)
        } catch (e: Exception) {
            Log.e("EventRepository", "Roster sync interrupted", e)
            Result.failure(e)
        }
    }

    private fun mapToAttendeeEntity(eventId: Long, dto: RosterItemResponse): AttendeeEntity {
        return AttendeeEntity(
            localId = 0,
            eventId = eventId,
            attendeeId = dto.attendeeId,
            identity = dto.identity,
            firstName = dto.firstName,
            lastName = dto.lastName
        )
    }

    suspend fun retryFailedEntries(eventId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            entryDao.resetFailedToPendingForEvent(eventId)
            syncEntries()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun primeLastScannedIdentifier(eventId: Long) {
        val lastEntry = entryDao.findMostRecentEntryForEvent(eventId)
        lastScannedIdentity = lastEntry?.snapshotIdentity
    }

    suspend fun processScan(eventId: Long, identity: String): ScanResult {
        val attendee = attendeeDao.findAttendeeByIdentity(eventId, identity)
            ?: return ScanResult.AttendeeNotFound

        if (identity == lastScannedIdentity) {
            return ScanResult.AlreadyScanned(attendee)
        }

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
        syncMutex.withLock {
            var totalSuccess = 0

            try {
                // Keep processing batches until the database returns no more pending items
                while (true) {
                    // 1. Get Batch (Limit 50 to prevent timeouts)
                    val batch = entryDao.getPendingEntriesBatch(50)
                    if (batch.isEmpty()) {
                        break // Queue is empty, we are done
                    }

                    // 2. Prepare Network Request
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

                    // 3. Network Call
                    val response = apiService.syncEntries(request)

                    // 4. Process Response
                    val allUuids = batch.map { it.scanUuid }
                    val failedUuids = response.failedUuids.toSet()
                    val successUuids = allUuids.filter { !failedUuids.contains(it) }

                    // A. Handle Successes
                    if (successUuids.isNotEmpty()) {
                        entryDao.markAsSyncedByUuid(successUuids)
                        totalSuccess += successUuids.size
                    }

                    // B. Handle Explicit Failures (Server said "No")
                    if (failedUuids.isNotEmpty()) {
                        entryDao.markAsFailedByUuid(failedUuids.toList(), "Server Rejected")
                    }
                }

                Result.success(totalSuccess)

            } catch (e: Exception) {
                // 5. Handle Network/Crash Errors
                // If the network dies mid-loop, we grab the current pending batch
                // and mark them as failed (with retry count increment) so the UI shows the error.
                val pendingBatch = entryDao.getPendingEntriesBatch(50)
                val batchUuids = pendingBatch.map { it.scanUuid }

                if (batchUuids.isNotEmpty()) {
                    val errorMessage = e.message ?: "Unknown Sync Error"
                    entryDao.markAsFailedByUuid(batchUuids, errorMessage)
                }

                if (e is HttpException && e.code() in 400..499) {
                    // 4xx errors are likely permanent (Bug/Data issue), return as failure but custom message
                    Result.failure(Exception("Sync Rejected by Server (Code ${e.code()}). Marked as Failed."))
                } else {
                    // 5xx or Network errors: Return failure so WorkManager retries with backoff
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun runHousekeeping() = withContext(Dispatchers.IO) {
        try {
            val threshold = Instant.now().minus(24, ChronoUnit.HOURS).toEpochMilli()
            val count = entryDao.deleteSyncedEntriesOlderThan(threshold)
            if (count > 0) Log.i("EventRepository", "Housekeeping: Deleted $count entries.")
        } catch (e: Exception) {
            Log.e("EventRepository", "Housekeeping failed", e)
        }
    }

    // FIX: Added 'limit' parameter
    fun getScannedItemsStream(eventId: Long, limit: Int): Flow<List<ScannedItemUi>> {
        return entryDao.getEntriesForEventStream(eventId, limit).map { mapEntriesToUi(it) }
    }

    fun searchScannedItems(eventId: Long, query: String): Flow<List<ScannedItemUi>> {
        return entryDao.searchEntries(eventId, query).map { mapEntriesToUi(it) }
    }

    private fun mapEntriesToUi(entries: List<EntryEntity>): List<ScannedItemUi> {
        return entries.map { entry ->
            ScannedItemUi(
                id = entry.id.toLong(),
                name = "${entry.snapshotLastName}, ${entry.snapshotFirstName}",
                identity = entry.snapshotIdentity,
                isSynced = entry.syncStatus == SyncStatus.SYNCED,
                isFailed = entry.syncStatus == SyncStatus.FAILED
            )
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

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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

    // MUTEX: Prevents "Double-Click" bandwidth waste
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
            // 1-hour buffer logic
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

            val attendeesByEvent = mutableMapOf<Long, List<AttendeeEntity>>()

            remoteEvents.map { event ->
                async {
                    attendeesByEvent[event.id] = fetchAllAttendeesFromApi(event.id)
                }
            }.awaitAll()

            appDatabase.withTransaction {
                eventDao.clearAll()
                eventDao.insertAll(eventEntities)

                // WIPE AND REPLACE STRATEGY
                remoteEvents.forEach { event ->
                    attendeeDao.clearAttendeesForEvent(event.id)
                }

                attendeesByEvent.values.forEach {
                    if (it.isNotEmpty()) attendeeDao.insertAll(it)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Refresh failed", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchAllAttendeesFromApi(eventId: Long): List<AttendeeEntity> {
        val all = mutableListOf<AttendeeEntity>()
        var page = 0
        var isLast = false
        while (!isLast) {
            val res = apiService.getAttendeesForEvent(eventId, page, 500)
            all.addAll(res.content.map {
                // Mapping API response to Local Entity
                // Note: qrCodeHash is removed from entity, so we don't map it
                AttendeeEntity(
                    localId = 0,
                    eventId = eventId,
                    attendeeId = it.attendeeId,
                    identity = it.identity,
                    firstName = it.firstName,
                    lastName = it.lastName
                )
            })
            isLast = res.last
            page++
        }
        return all
    }

    suspend fun primeLastScannedIdentifier(eventId: Long) {
        val lastEntry = entryDao.findMostRecentEntryForEvent(eventId)
        lastScannedIdentity = lastEntry?.snapshotIdentity
    }

    // THIN CLIENT SCAN LOGIC
    // Removed: sessionId argument (Backend figures it out)
    suspend fun processScan(eventId: Long, identity: String): ScanResult {
        if (identity == lastScannedIdentity) return ScanResult.AlreadyScanned

        // 1. Lookup
        val attendee = attendeeDao.findAttendeeByIdentity(eventId, identity)
            ?: return ScanResult.AttendeeNotFound

        // 2. Snapshot
        val entry = EntryEntity(
            eventId = eventId,
            attendeeId = attendee.attendeeId,
            // Copy details to preserve them even if Roster is wiped later
            snapshotIdentity = attendee.identity,
            snapshotFirstName = attendee.firstName,
            snapshotLastName = attendee.lastName,
            scanTimestamp = Instant.now(),
            syncStatus = SyncStatus.PENDING
        )

        entryDao.insert(entry)
        lastScannedIdentity = identity

        // Return success (We don't calculate Late/Early here anymore)
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

                // 1. Call API
                val response = apiService.syncEntries(request)

                // 2. Calculate Lists
                val allUuids = batch.map { it.scanUuid }
                val failedUuids = response.failedUuids.toSet()

                // Success = All - Failed
                val successUuids = allUuids.filter { !failedUuids.contains(it) }

                // 3. Update Local DB
                if (successUuids.isNotEmpty()) {
                    entryDao.markAsSyncedByUuid(successUuids)
                }

                if (failedUuids.isNotEmpty()) {
                    // These are Poison Pills. Mark as FAILED so we stop retrying them.
                    entryDao.markAsFailedByUuid(failedUuids.toList())
                }

                // 4. Recurse if we had a full successful batch
                if (batch.size == 50 && failedUuids.isEmpty()) {
                    syncEntries()
                }

                Result.success(response.successCount)
            } catch (e: Exception) {
                // Network error: Everything stays PENDING, will retry next time
                Result.failure(e)
            }
        }
    }

    fun getScannedAttendeesStream(eventId: Long): Flow<List<AttendeeEntity>> {
        // Reads directly from Entry table (Snapshots), so it never breaks on refresh
        return entryDao.getEntriesForEventStream(eventId).map { entries ->
            entries.map {
                AttendeeEntity(
                    localId = 0,
                    eventId = it.eventId,
                    attendeeId = it.attendeeId,
                    identity = it.snapshotIdentity,
                    firstName = it.snapshotFirstName,
                    lastName = it.snapshotLastName
                )
            }
        }
    }
}

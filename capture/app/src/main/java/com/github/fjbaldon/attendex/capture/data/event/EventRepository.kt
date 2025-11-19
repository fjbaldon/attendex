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
import com.github.fjbaldon.attendex.capture.core.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.core.data.remote.EntrySyncRequest
import com.github.fjbaldon.attendex.capture.core.data.remote.SessionResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase,
    private val eventDao: EventDao,
    private val attendeeDao: AttendeeDao,
    private val entryDao: EntryDao
) {
    private var lastScannedIdentifier: String? = null

    val hasUnsyncedRecords: Flow<Boolean> = entryDao.getUnsyncedEntryCount()
        .map { count -> count > 0 }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventNameById(eventId: Long): String? {
        return eventDao.getEventById(eventId)?.name
    }

    suspend fun getSessionsForEvent(eventId: Long): List<SessionResponse> {
        return eventDao.getEventById(eventId)?.sessions ?: emptyList()
    }

    suspend fun isEventActive(eventId: Long): Boolean {
        val event = eventDao.getEventById(eventId) ?: return false
        val now = Instant.now()
        return try {
            val start = Instant.parse(event.startDate)
            val end = Instant.parse(event.endDate)
            now.isAfter(start.minusSeconds(3600)) && now.isBefore(end.plusSeconds(3600))
        } catch (e: Exception) {
            Log.e("EventRepository", "Failed to parse event dates", e)
            true
        }
    }

    suspend fun refreshEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteEvents = apiService.getActiveEvents()
            val eventEntities = remoteEvents.map {
                EventEntity(
                    id = it.id,
                    name = it.name,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    sessions = it.sessions
                )
            }

            appDatabase.withTransaction {
                eventDao.clearAll()
                eventDao.insertAll(eventEntities)
            }

            remoteEvents.map { event ->
                async { refreshAttendeesForEvent(event.id) }
            }.awaitAll()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Failed to refresh events", e)
            Result.failure(e)
        }
    }

    private suspend fun refreshAttendeesForEvent(eventId: Long): Result<Unit> {
        return try {
            var page = 0
            val pageSize = 500 // Safe batch size for mobile memory
            var isLastPage = false

            // Clear existing data first
            appDatabase.withTransaction {
                attendeeDao.clearAttendeesForEvent(eventId)
            }

            while (!isLastPage) {
                val response = apiService.getAttendeesForEvent(eventId, page, pageSize)

                val attendeeEntities = response.content.map {
                    AttendeeEntity(
                        eventId = eventId,
                        attendeeId = it.attendeeId,
                        uniqueIdentifier = it.uniqueIdentifier,
                        qrCodeHash = it.qrCodeHash,
                        firstName = it.firstName,
                        lastName = it.lastName
                    )
                }

                if (attendeeEntities.isNotEmpty()) {
                    appDatabase.withTransaction {
                        attendeeDao.insertAll(attendeeEntities)
                    }
                }

                isLastPage = response.last
                page++
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("EventRepository", "Failed to fetch attendees for event $eventId", e)
            Result.failure(e)
        }
    }

    suspend fun primeLastScannedIdentifier(eventId: Long) {
        val mostRecentEntry = entryDao.findMostRecentEntryForEvent(eventId)
        if (mostRecentEntry != null) {
            val lastAttendee = attendeeDao.findAttendeesByIds(listOf(mostRecentEntry.attendeeId)).firstOrNull()
            lastScannedIdentifier = lastAttendee?.uniqueIdentifier
        } else {
            lastScannedIdentifier = null
        }
    }

    suspend fun processScan(eventId: Long, sessionId: Long, identifier: String): ScanResult {
        if (identifier == lastScannedIdentifier) {
            return ScanResult.AlreadyScanned
        }

        val attendee = attendeeDao.findAttendeeByIdentifier(eventId, identifier)
            ?: return ScanResult.AttendeeNotFound

        val entry = EntryEntity(
            eventId = eventId,
            sessionId = sessionId,
            attendeeId = attendee.attendeeId,
            scanTimestamp = Instant.now(),
            isSynced = false
        )

        entryDao.insert(entry)
        lastScannedIdentifier = identifier

        return ScanResult.Success(attendee)
    }

    suspend fun syncEntries(): Result<Int> = withContext(Dispatchers.IO) {
        var totalSynced = 0
        val batchSize = 50

        try {
            while (true) {
                val batch = entryDao.getUnsyncedEntriesBatch(batchSize)
                if (batch.isEmpty()) {
                    break
                }

                val request = EntrySyncRequest(
                    records = batch.map {
                        EntrySyncRequest.EntryRecord(
                            sessionId = it.sessionId,
                            attendeeId = it.attendeeId,
                            // FIXED: Changed from checkInTimestamp to scanTimestamp
                            scanTimestamp = it.scanTimestamp.toString()
                        )
                    }
                )

                apiService.syncEntries(request)
                entryDao.markAsSynced(batch.map { it.id })
                totalSynced += batch.size
            }
            Result.success(totalSynced)
        } catch (e: Exception) {
            Log.e("EventRepository", "Sync failed", e)
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getScannedAttendeesStream(eventId: Long): Flow<List<AttendeeEntity>> {
        return entryDao.getEntriesForEventStream(eventId)
            .flatMapLatest { entries ->
                val attendeeIds = entries.map { it.attendeeId }.distinct()
                if (attendeeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    attendeeDao.getAttendeesByIds(attendeeIds)
                }
            }
    }
}

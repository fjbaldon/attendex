package com.github.fjbaldon.attendex.capture.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.github.fjbaldon.attendex.capture.data.local.AppDatabase
import com.github.fjbaldon.attendex.capture.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.capture.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.capture.data.local.dao.EventDao
import com.github.fjbaldon.attendex.capture.data.local.model.AttendanceRecordEntity
import com.github.fjbaldon.attendex.capture.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.capture.data.local.model.EventEntity
import com.github.fjbaldon.attendex.capture.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.data.remote.AttendanceSyncRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

sealed class ScanResult {
    data class Success(val attendee: AttendeeEntity) : ScanResult()
    data object AttendeeNotFound : ScanResult()
    data object AlreadyScanned : ScanResult()
}

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase,
    private val eventDao: EventDao,
    private val attendeeDao: AttendeeDao,
    private val attendanceRecordDao: AttendanceRecordDao
) {
    private var lastScannedIdentifier: String? = null

    val hasUnsyncedRecords: Flow<Boolean> = attendanceRecordDao.getUnsyncedRecordCount()
        .map { count -> count > 0 }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventNameById(eventId: Long): String? {
        return eventDao.getEventById(eventId)?.eventName
    }

    suspend fun isEventActive(eventId: Long): Boolean {
        val event = eventDao.getEventById(eventId) ?: return false
        val now = Instant.now()
        try {
            val start = Instant.parse(event.startDate)
            val end = Instant.parse(event.endDate)
            return now.isAfter(start) && now.isBefore(end)
        } catch (e: Exception) {
            Log.e("EventRepository", "Failed to parse event start/end dates", e)
            return false
        }
    }

    suspend fun refreshEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteEvents = apiService.getActiveEvents()
            val eventEntities = remoteEvents.map {
                EventEntity(it.id, it.eventName, it.startDate, it.endDate, it.timeSlots)
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
            Log.e("EventRepository", "Failed to refresh events and rosters", e)
            Result.failure(e)
        }
    }

    suspend fun refreshAttendeesForEvent(eventId: Long): Result<Unit> {
        return try {
            val remoteAttendees = apiService.getAttendeesForEvent(eventId)
            val attendeeEntities = remoteAttendees.map {
                AttendeeEntity(
                    eventId = eventId,
                    attendeeId = it.attendeeId,
                    uniqueIdentifier = it.uniqueIdentifier,
                    qrCodeHash = it.qrCodeHash,
                    firstName = it.firstName,
                    lastName = it.lastName
                )
            }

            appDatabase.withTransaction {
                attendeeDao.clearAttendeesForEvent(eventId)
                attendeeDao.insertAll(attendeeEntities)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun primeLastScannedIdentifier(eventId: Long) {
        val mostRecentRecord = attendanceRecordDao.findMostRecentRecordForEvent(eventId)
        if (mostRecentRecord != null) {
            val lastAttendee = attendeeDao.findAttendeesByIds(listOf(mostRecentRecord.attendeeId)).firstOrNull()
            lastScannedIdentifier = lastAttendee?.uniqueIdentifier
        } else {
            lastScannedIdentifier = null
        }
    }

    suspend fun processScan(eventId: Long, identifier: String): ScanResult {
        if (identifier == lastScannedIdentifier) {
            return ScanResult.AlreadyScanned
        }

        val attendee = attendeeDao.findAttendeeByIdentifier(eventId, identifier)
            ?: return ScanResult.AttendeeNotFound

        val record = AttendanceRecordEntity(
            eventId = eventId,
            attendeeId = attendee.attendeeId,
            checkInTimestamp = Instant.now(),
            isSynced = false
        )
        attendanceRecordDao.insert(record)

        lastScannedIdentifier = identifier

        return ScanResult.Success(attendee)
    }

    suspend fun syncAttendanceRecords(): Result<Int> {
        var totalSynced = 0
        val batchSize = 100

        try {
            while (true) {
                val batch = attendanceRecordDao.getUnsyncedRecordsBatch(batchSize)
                if (batch.isEmpty()) {
                    break
                }

                val request = AttendanceSyncRequest(
                    records = batch.map {
                        AttendanceSyncRequest.Record(
                            eventId = it.eventId,
                            attendeeId = it.attendeeId,
                            checkInTimestamp = it.checkInTimestamp.toString()
                        )
                    }
                )

                apiService.syncAttendance(request)

                attendanceRecordDao.markAsSynced(batch.map { it.id })

                totalSynced += batch.size
            }
            return Result.success(totalSynced)
        } catch (e: Exception) {
            Log.e("EventRepository", "Sync failed after syncing $totalSynced records.", e)
            return Result.failure(e)
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getScannedAttendeesStream(eventId: Long): Flow<List<AttendeeEntity>> {
        return attendanceRecordDao.getRecordsForEventStream(eventId)
            .flatMapLatest { records ->
                val attendeeIds = records.map { it.attendeeId }.distinct()
                if (attendeeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    attendeeDao.getAttendeesByIds(attendeeIds)
                }
            }
    }
}

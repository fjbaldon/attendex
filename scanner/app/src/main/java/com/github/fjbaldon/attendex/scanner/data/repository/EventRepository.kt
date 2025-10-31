package com.github.fjbaldon.attendex.scanner.data.repository

import android.util.Log
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.EventDao
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendanceRecordEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.EventEntity
import com.github.fjbaldon.attendex.scanner.data.remote.ApiService
import com.github.fjbaldon.attendex.scanner.data.remote.AttendanceSyncRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

sealed class ScanResult {
    data class Success(val attendee: AttendeeEntity) : ScanResult()
    data object AttendeeNotFound : ScanResult()
    data object AlreadyScanned : ScanResult()
}

class EventRepository @Inject constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao,
    private val attendeeDao: AttendeeDao,
    private val attendanceRecordDao: AttendanceRecordDao
) {
    val hasUnsyncedRecords: Flow<Boolean> = attendanceRecordDao.getUnsyncedRecordCount()
        .map { count -> count > 0 }

    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun getEventNameById(eventId: Long): String? {
        return eventDao.getEventById(eventId)?.eventName
    }

    suspend fun refreshEvents(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val remoteEvents = apiService.getActiveEvents()
            val eventEntities = remoteEvents.map { EventEntity(it.id, it.eventName, it.timeSlots) }

            eventDao.clearAll()
            eventDao.insertAll(eventEntities)

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
            attendeeDao.clearAttendeesForEvent(eventId)
            attendeeDao.insertAll(attendeeEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processScan(eventId: Long, identifier: String, scanType: String): ScanResult {
        val attendee = attendeeDao.findAttendeeByIdentifier(eventId, identifier)
            ?: return ScanResult.AttendeeNotFound

        val record = AttendanceRecordEntity(
            eventId = eventId,
            attendeeId = attendee.attendeeId,
            checkInTimestamp = Instant.now(),
            isSynced = false,
            type = scanType
        )
        attendanceRecordDao.insert(record)
        return ScanResult.Success(attendee)
    }

    suspend fun syncAttendanceRecords(): Result<Int> {
        return try {
            val unsyncedRecords = attendanceRecordDao.getUnsyncedRecords()
            if (unsyncedRecords.isEmpty()) return Result.success(0)

            val request = AttendanceSyncRequest(
                records = unsyncedRecords.map {
                    AttendanceSyncRequest.Record(
                        eventId = it.eventId,
                        attendeeId = it.attendeeId,
                        checkInTimestamp = it.checkInTimestamp.toString(),
                        type = it.type
                    )
                }
            )
            apiService.syncAttendance(request)
            attendanceRecordDao.markAsSynced(unsyncedRecords.map { it.id })
            Result.success(unsyncedRecords.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun determineScanType(eventId: Long): String? {
        val event = eventDao.getEventById(eventId) ?: return null
        val now = Instant.now()

        val activeSlot = event.timeSlots.find { slot ->
            try {
                val startTime = Instant.parse(slot.startTime)
                val endTime = Instant.parse(slot.endTime)
                now.isAfter(startTime) && now.isBefore(endTime)
            } catch (e: Exception) {
                Log.e("EventRepository", "Failed to parse time slot dates", e)
                false
            }
        }
        return activeSlot?.type
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getScannedAttendeesStream(eventId: Long, type: String): Flow<List<AttendeeEntity>> {
        return attendanceRecordDao.getRecordsForEventStream(eventId, type)
            .flatMapLatest { records ->
                val attendeeIds = records.map { it.attendeeId }
                if (attendeeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    attendeeDao.getAttendeesByIds(attendeeIds)
                }
            }
    }
}

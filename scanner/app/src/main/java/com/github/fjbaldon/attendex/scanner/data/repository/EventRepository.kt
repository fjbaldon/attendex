package com.github.fjbaldon.attendex.scanner.data.repository

import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendanceRecordDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.AttendeeDao
import com.github.fjbaldon.attendex.scanner.data.local.dao.EventDao
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendanceRecordEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.AttendeeEntity
import com.github.fjbaldon.attendex.scanner.data.local.model.EventEntity
import com.github.fjbaldon.attendex.scanner.data.remote.ApiService
import com.github.fjbaldon.attendex.scanner.data.remote.AttendanceSyncRequest
import kotlinx.coroutines.flow.Flow
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
    fun getEvents(): Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun refreshEvents(): Result<Unit> {
        return try {
            val remoteEvents = apiService.getActiveEvents()
            val eventEntities = remoteEvents.map { EventEntity(it.id, it.eventName) }
            eventDao.clearAll()
            eventDao.insertAll(eventEntities)
            Result.success(Unit)
        } catch (e: Exception) {
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
                    qrCodeHash = it.qrCodeHash
                )
            }
            attendeeDao.clearAttendeesForEvent(eventId)
            attendeeDao.insertAll(attendeeEntities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processScan(eventId: Long, qrCodeHash: String): ScanResult {
        val attendee = attendeeDao.findAttendeeByQrCode(eventId, qrCodeHash)
            ?: return ScanResult.AttendeeNotFound

        val record = AttendanceRecordEntity(
            eventId = eventId,
            attendeeId = attendee.attendeeId,
            checkInTimestamp = Instant.now(),
            isSynced = false
        )
        attendanceRecordDao.insert(record)
        return ScanResult.Success(attendee)
    }

    suspend fun syncAttendanceRecords(): Result<Int> {
        return try {
            val unsyncedRecords = attendanceRecordDao.getUnsyncedRecords()
            if (unsyncedRecords.isEmpty()) {
                return Result.success(0)
            }

            val request = AttendanceSyncRequest(
                records = unsyncedRecords.map {
                    AttendanceSyncRequest.Record(
                        eventId = it.eventId,
                        attendeeId = it.attendeeId,
                        checkInTimestamp = it.checkInTimestamp.toString()
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
}

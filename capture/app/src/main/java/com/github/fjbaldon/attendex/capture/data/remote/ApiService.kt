package com.github.fjbaldon.attendex.capture.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): AuthResponse

    @GET("api/v1/app/events")
    suspend fun getActiveEvents(): List<ActiveEventResponse>

    @GET("api/v1/app/events/{eventId}/attendees")
    suspend fun getAttendeesForEvent(@Path("eventId") eventId: Long): List<EventAttendeeSyncResponse>

    @POST("api/v1/app/attendance/sync")
    suspend fun syncAttendance(@Body syncRequest: AttendanceSyncRequest)
}

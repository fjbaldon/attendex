package com.github.fjbaldon.attendex.capture.core.data.remote

import retrofit2.http.*

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): AuthResponse

    @POST("api/v1/users/me/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest)

    @GET("api/v1/capture/events")
    suspend fun getActiveEvents(): List<ActiveEventResponse>

    @GET("api/v1/capture/events/{eventId}/attendees")
    suspend fun getAttendeesForEvent(
        @Path("eventId") eventId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): PaginatedResponse<RosterItemResponse>

    @POST("api/v1/capture/sync")
    suspend fun syncEntries(@Body syncRequest: EntrySyncRequest): BatchSyncResponse
}

@kotlinx.serialization.Serializable
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val last: Boolean
)

@kotlinx.serialization.Serializable
data class ChangePasswordRequest(val newPassword: String)

package com.github.fjbaldon.attendex.scanner.data.repository

import com.github.fjbaldon.attendex.scanner.data.auth.SessionManager
import com.github.fjbaldon.attendex.scanner.data.remote.ApiService
import com.github.fjbaldon.attendex.scanner.data.remote.AuthRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    fun isLoggedIn(): Boolean = sessionManager.authToken != null

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val request = AuthRequest(email, password)
            val response = apiService.login(request)
            sessionManager.authToken = response.accessToken
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        sessionManager.clear()
    }
}

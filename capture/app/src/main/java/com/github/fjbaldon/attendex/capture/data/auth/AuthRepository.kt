package com.github.fjbaldon.attendex.capture.data.auth

import android.util.Base64
import com.github.fjbaldon.attendex.capture.core.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.core.data.remote.AuthRequest
import com.github.fjbaldon.attendex.capture.core.data.remote.ChangePasswordRequest
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    val isLoggedInFlow: StateFlow<Boolean> = sessionManager.isLoggedInFlow

    fun isLoggedIn(): Boolean = sessionManager.authToken != null

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val request = AuthRequest(email, password)
            val response = apiService.login(request)
            sessionManager.authToken = response.accessToken
            LoginResult.Success
        } catch (e: Exception) {
            when (e) {
                is IOException -> LoginResult.NetworkError
                is HttpException -> {
                    if (e.code() == 401) {
                        LoginResult.InvalidCredentials
                    } else {
                        LoginResult.GenericError("Server error: ${e.code()}")
                    }
                }

                else -> LoginResult.GenericError(e.message)
            }
        }
    }

    suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            apiService.changePassword(ChangePasswordRequest(newPassword))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isPasswordChangeRequired(): Boolean {
        val token = sessionManager.authToken ?: return false
        try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            return json.optBoolean("forcePasswordChange", false)
        } catch (_: Exception) {
            return false
        }
    }

    fun logout() {
        sessionManager.clear()
    }
}

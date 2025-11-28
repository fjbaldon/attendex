package com.github.fjbaldon.attendex.capture.data.auth

import android.content.Context
import android.util.Base64
import androidx.work.WorkManager
import com.github.fjbaldon.attendex.capture.core.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.core.data.remote.AuthRequest
import com.github.fjbaldon.attendex.capture.core.data.remote.ChangePasswordRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    @param:ApplicationContext private val context: Context
) {
    val isLoggedInFlow: StateFlow<Boolean> = sessionManager.isLoggedInFlow

    fun isLoggedIn(): Boolean = sessionManager.authToken != null

    suspend fun login(email: String, password: String): LoginResult {
        return try {
            val request = AuthRequest(email, password)
            val response = apiService.login(request)

            if (!hasScannerRole(response.accessToken)) {
                return LoginResult.GenericError("Access Denied: This app is for Scanners only. Organizers must use the Web Dashboard.")
            }

            sessionManager.authToken = response.accessToken

            try {
                val org = apiService.getMyOrganization()
                sessionManager.identityRegex = org.identityFormatRegex
            } catch (e: Exception) {
                e.printStackTrace()
            }

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

    private fun hasScannerRole(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)

            val rolesArray = json.optJSONArray("roles") ?: return false

            for (i in 0 until rolesArray.length()) {
                if (rolesArray.getString(i) == "ROLE_SCANNER") {
                    return true
                }
            }
            return false
        } catch (_: Exception) {
            return false
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
        WorkManager.getInstance(context).cancelUniqueWork("AttendEx_Periodic_Sync")
        WorkManager.getInstance(context).cancelUniqueWork("AttendEx_Immediate_Sync")
    }
}

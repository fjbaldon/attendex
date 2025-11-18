package com.github.fjbaldon.attendex.capture.data.repository

import com.github.fjbaldon.attendex.capture.data.auth.SessionManager
import com.github.fjbaldon.attendex.capture.data.remote.ApiService
import com.github.fjbaldon.attendex.capture.data.remote.AuthRequest
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object NetworkError : LoginResult()
    data class GenericError(val message: String?) : LoginResult()
}

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

    fun logout() {
        sessionManager.clear()
    }
}

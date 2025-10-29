package com.github.fjbaldon.attendex.scanner.data.repository

import com.github.fjbaldon.attendex.scanner.data.auth.PasswordHasher
import com.github.fjbaldon.attendex.scanner.data.auth.SessionManager
import com.github.fjbaldon.attendex.scanner.data.local.dao.UserCredentialsDao
import com.github.fjbaldon.attendex.scanner.data.local.model.UserCredentialsEntity
import com.github.fjbaldon.attendex.scanner.data.remote.ApiService
import com.github.fjbaldon.attendex.scanner.data.remote.AuthRequest
import com.github.fjbaldon.attendex.scanner.di.NetworkConnectivityService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object NetworkError : LoginResult()
    data class GenericError(val message: String?) : LoginResult()
    data object FirstLoginOfflineError : LoginResult()
}

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val userCredentialsDao: UserCredentialsDao,
    private val passwordHasher: PasswordHasher,
    private val networkConnectivityService: NetworkConnectivityService
) {
    fun isLoggedIn(): Boolean =
        sessionManager.authToken != null || sessionManager.currentUserEmail != null

    suspend fun login(email: String, password: String): LoginResult {
        if (networkConnectivityService.isOnline()) {
            val onlineResult = attemptOnlineLogin(email, password)
            if (onlineResult is LoginResult.Success) {
                val hashedPassword = passwordHasher.hashPassword(password)
                userCredentialsDao.saveCredentials(UserCredentialsEntity(email, hashedPassword))
                sessionManager.currentUserEmail = email
            }
            return onlineResult
        } else {
            return attemptOfflineLogin(email, password)
        }
    }

    private suspend fun attemptOnlineLogin(email: String, password: String): LoginResult {
        return try {
            val request = AuthRequest(email, password)
            val response = apiService.login(request)
            sessionManager.authToken = response.accessToken
            LoginResult.Success
        } catch (e: Exception) {
            when (e) {
                is IOException -> LoginResult.NetworkError
                is HttpException -> if (e.code() == 401) LoginResult.InvalidCredentials else LoginResult.GenericError(
                    "Server Error: ${e.code()}"
                )

                else -> LoginResult.GenericError(e.message)
            }
        }
    }

    private suspend fun attemptOfflineLogin(email: String, password: String): LoginResult {
        val storedCredentials = userCredentialsDao.findByEmail(email)
            ?: return LoginResult.FirstLoginOfflineError

        val enteredPasswordHash = passwordHasher.hashPassword(password)

        return if (enteredPasswordHash == storedCredentials.hashedPassword) {
            sessionManager.currentUserEmail = email
            LoginResult.Success
        } else {
            LoginResult.InvalidCredentials
        }
    }

    fun logout() {
        sessionManager.clear()
    }
}

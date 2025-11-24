package com.github.fjbaldon.attendex.capture.feature.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.auth.LoginResult
import com.github.fjbaldon.attendex.capture.data.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val navigateToEvents: Boolean = false, // Explicit success path
    val navigateToLogin: Boolean = false,  // Explicit logout/fallback path
    val error: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun changePassword(newPassword: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val changeResult = authRepository.changePassword(newPassword)

            if (changeResult.isSuccess) {
                // Try to auto-login to get a fresh token
                val email = sessionManager.lastUserEmail

                if (email != null) {
                    val loginResult = authRepository.login(email, newPassword)
                    if (loginResult is LoginResult.Success) {
                        // SUCCESS: Direct to Events
                        _uiState.update { it.copy(isLoading = false, navigateToEvents = true) }
                        return@launch
                    }
                }

                // FALLBACK: Logout and direct to Login
                authRepository.logout()
                _uiState.update { it.copy(isLoading = false, navigateToLogin = true) }
            } else {
                val exception = changeResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception?.message ?: "Failed to change password. Please try again."
                    )
                }
            }
        }
    }

    fun logoutAndCancel() {
        authRepository.logout()
        _uiState.update { it.copy(navigateToLogin = true) }
    }
}

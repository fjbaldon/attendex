package com.github.fjbaldon.attendex.capture.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import com.github.fjbaldon.attendex.capture.data.auth.LoginResult
import com.github.fjbaldon.attendex.capture.data.auth.SessionManager
import com.github.fjbaldon.attendex.capture.data.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val requirePasswordChange: Boolean = false,
    val error: String? = null,
    // NEW: Dialog Trigger
    val showUserMismatchDialog: Boolean = false,
    val pendingEmail: String? = null,
    val pendingPassword: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository, // Inject this
    private val sessionManager: SessionManager    // Inject this
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty.") }
            return
        }

        viewModelScope.launch {
            val lastEmail = sessionManager.lastUserEmail
            val unsyncedCount = eventRepository.getUnsyncedCount()

            // QUARANTINE CHECK:
            // If a DIFFERENT user is logging in AND we have unsynced data...
            if (lastEmail != null && !lastEmail.equals(email, ignoreCase = true) && unsyncedCount > 0) {
                // Stop! Ask the user what to do.
                _uiState.update {
                    it.copy(
                        showUserMismatchDialog = true,
                        pendingEmail = email,
                        pendingPassword = password
                    )
                }
                return@launch
            }

            // If safe, proceed
            performLogin(email, password)
        }
    }

    fun onConfirmWipeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(showUserMismatchDialog = false, isLoading = true) }

            // Wipe the old user's data
            eventRepository.clearAllLocalData()

            // Proceed with login for the new user
            val email = _uiState.value.pendingEmail ?: return@launch
            val password = _uiState.value.pendingPassword ?: return@launch
            performLogin(email, password)
        }
    }

    fun onCancelWipe() {
        _uiState.update {
            it.copy(
                showUserMismatchDialog = false,
                pendingEmail = null,
                pendingPassword = null
            )
        }
    }

    private suspend fun performLogin(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        when (val result = authRepository.login(email, password)) {
            is LoginResult.Success -> {
                // Save this email as the "owner" of the device data
                sessionManager.lastUserEmail = email

                if (authRepository.isPasswordChangeRequired()) {
                    _uiState.update { it.copy(isLoading = false, requirePasswordChange = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
            }

            is LoginResult.InvalidCredentials -> {
                _uiState.update { it.copy(isLoading = false, error = "Invalid email or password.") }
            }

            is LoginResult.NetworkError -> {
                _uiState.update { it.copy(isLoading = false, error = "Cannot connect to server.") }
            }

            is LoginResult.GenericError -> {
                _uiState.update { it.copy(isLoading = false, error = result.message ?: "An unknown error occurred.") }
            }
        }
    }
}

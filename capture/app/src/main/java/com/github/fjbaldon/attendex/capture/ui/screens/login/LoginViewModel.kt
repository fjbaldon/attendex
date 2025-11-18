package com.github.fjbaldon.attendex.capture.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.data.repository.AuthRepository
import com.github.fjbaldon.attendex.capture.data.repository.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.login(email, password)

            when (result) {
                is LoginResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }

                is LoginResult.InvalidCredentials -> {
                    _uiState.update { it.copy(isLoading = false, error = "Invalid email or password.") }
                }

                is LoginResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Cannot connect to server. Please ensure your device has network access."
                        )
                    }
                }

                is LoginResult.GenericError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "An unknown error occurred."
                        )
                    }
                }
            }
        }
    }
}

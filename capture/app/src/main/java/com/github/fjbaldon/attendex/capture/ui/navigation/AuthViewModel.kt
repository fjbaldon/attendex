package com.github.fjbaldon.attendex.capture.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val isLoggedInFlow: StateFlow<Boolean> = authRepository.isLoggedInFlow

    val requirePasswordChange: StateFlow<Boolean> = authRepository.isLoggedInFlow
        .map { isLoggedIn ->
            isLoggedIn && authRepository.isPasswordChangeRequired()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
}

package com.github.fjbaldon.attendex.capture.ui.navigation

import androidx.lifecycle.ViewModel
import com.github.fjbaldon.attendex.capture.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val isLoggedInFlow: StateFlow<Boolean> = authRepository.isLoggedInFlow
}

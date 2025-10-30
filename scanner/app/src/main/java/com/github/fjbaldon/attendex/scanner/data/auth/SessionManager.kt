package com.github.fjbaldon.attendex.scanner.data.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(hasActiveSession())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private fun hasActiveSession(): Boolean {
        return prefs.getString(KEY_AUTH_TOKEN, null) != null
    }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs.edit { putString(KEY_AUTH_TOKEN, value) }
            _isLoggedIn.value = hasActiveSession()
        }

    fun clear() {
        prefs.edit { clear() }
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}

package com.github.fjbaldon.attendex.capture.data.auth

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

    var lastUserEmail: String?
        get() = prefs.getString(KEY_LAST_EMAIL, null)
        set(value) {
            prefs.edit { putString(KEY_LAST_EMAIL, value) }
        }

    // This is the property you added
    var identityRegex: String?
        get() = prefs.getString(KEY_IDENTITY_REGEX, null)
        set(value) {
            prefs.edit { putString(KEY_IDENTITY_REGEX, value) }
        }

    fun clear() {
        prefs.edit {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_IDENTITY_REGEX) // Clear regex on logout
        }
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_EMAIL = "last_email"
        // FIXED: Ensure this line is present
        private const val KEY_IDENTITY_REGEX = "identity_regex"
    }
}

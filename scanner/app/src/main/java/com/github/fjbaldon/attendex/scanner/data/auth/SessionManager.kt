package com.github.fjbaldon.attendex.scanner.data.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs.edit { putString(KEY_AUTH_TOKEN, value) }
        }

    var currentUserEmail: String?
        get() = prefs.getString(KEY_CURRENT_USER_EMAIL, null)
        set(value) {
            prefs.edit { putString(KEY_CURRENT_USER_EMAIL, value) }
        }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
    }
}

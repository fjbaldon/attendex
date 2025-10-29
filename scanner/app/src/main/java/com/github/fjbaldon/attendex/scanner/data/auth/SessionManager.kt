package com.github.fjbaldon.attendex.scanner.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs.edit { putString(KEY_AUTH_TOKEN, value) }
        }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}



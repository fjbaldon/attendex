package com.github.fjbaldon.attendex.scanner.data.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenProvider @Inject constructor(
    private val sessionManager: SessionManager
) {
    val authToken: String?
        get() = sessionManager.authToken
}

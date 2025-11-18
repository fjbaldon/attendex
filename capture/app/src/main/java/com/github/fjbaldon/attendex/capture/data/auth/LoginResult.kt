package com.github.fjbaldon.attendex.capture.data.auth

sealed class LoginResult {
    data object Success : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object NetworkError : LoginResult()
    data class GenericError(val message: String?) : LoginResult()
}

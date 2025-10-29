package com.github.fjbaldon.attendex.scanner.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String
)

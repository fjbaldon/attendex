package com.github.fjbaldon.attendex.capture.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

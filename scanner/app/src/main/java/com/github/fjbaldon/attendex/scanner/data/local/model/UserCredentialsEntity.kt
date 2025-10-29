package com.github.fjbaldon.attendex.scanner.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_credentials")
data class UserCredentialsEntity(
    @PrimaryKey val email: String,
    val hashedPassword: String
)

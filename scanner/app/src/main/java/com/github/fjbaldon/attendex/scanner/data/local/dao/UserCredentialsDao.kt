package com.github.fjbaldon.attendex.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.fjbaldon.attendex.scanner.data.local.model.UserCredentialsEntity

@Dao
interface UserCredentialsDao {
    @Upsert
    suspend fun saveCredentials(credentials: UserCredentialsEntity)

    @Query("SELECT * FROM user_credentials WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserCredentialsEntity?
}

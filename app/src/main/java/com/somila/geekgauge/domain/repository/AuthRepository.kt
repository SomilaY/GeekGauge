package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.enums.UserRole
import com.somila.geekgauge.domain.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User>
}
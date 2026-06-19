package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
}
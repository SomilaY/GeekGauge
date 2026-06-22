package com.somila.geekgauge.data.repository

import com.somila.geekgauge.data.mock.MockAuthDataSource
import com.somila.geekgauge.domain.enums.UserRole
import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val mockAuthDataSource: MockAuthDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        delay(1500)
        val user = mockAuthDataSource.login(email, password)
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> {
        delay(1500)
        return mockAuthDataSource.register(firstName, lastName, email, password, role)
    }
}
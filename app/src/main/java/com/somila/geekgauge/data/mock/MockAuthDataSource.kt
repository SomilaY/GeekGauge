package com.somila.geekgauge.data.mock

import com.somila.geekgauge.domain.enums.UserRole
import com.somila.geekgauge.domain.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthDataSource @Inject constructor() {

    private val users = listOf(
        User(
            id = "t001",
            firstName = "Sipho",
            lastName = "Dlamini",
            email = "sipho@institute.co.za",
            password = "trainer123",
            role = UserRole.TRAINER
        ),
        User(
            id = "g001",
            firstName = "Thandeka",
            lastName = "Nkosi",
            email = "thandeka@institute.co.za",
            password = "geek001",
            role = UserRole.GEEK,
            cohortId = "cohort_002"
        ),
        User(
            id = "g002",
            firstName = "Kabelo",
            lastName = "Mokoena",
            email = "kabelo@institute.co.za",
            password = "geek002",
            role = UserRole.GEEK,
            cohortId = "cohort_001"
        ),
        User(
            id = "g003",
            firstName = "Lerato",
            lastName = "Sithole",
            email = "lerato@institute.co.za",
            password = "geek003",
            role = UserRole.GEEK,
            cohortId = "cohort_001"
        ),
        User(
            id = "g004",
            firstName = "Thabo",
            lastName = "Khumalo",
            email = "thabo@institute.co.za",
            password = "geek004",
            role = UserRole.GEEK,
            cohortId = "cohort_002"
        )
    )

    fun login(email: String, password: String): User? {
        return users.firstOrNull {
            it.email == email && it.password == password
        }
    }

    fun getAllUsers(): List<User> = users

    fun getUserById(id: String): User? = users.firstOrNull { it.id == id }

    fun getGeeks(): List<User> = users.filter { it.role == UserRole.GEEK }
}
package com.somila.geekgauge.mock

import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.domain.enums.UserRole

class MockAuthDataSource {

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
        )
    )

    fun login(email: String, password: String): User? {
        return users.firstOrNull { it.email == email && it.password == password }
    }

    fun getAllUsers(): List<User> = users
}

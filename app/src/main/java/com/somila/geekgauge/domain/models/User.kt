package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.UserRole

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val cohortId: String? = null
)
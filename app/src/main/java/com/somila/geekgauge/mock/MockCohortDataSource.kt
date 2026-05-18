package com.somila.geekgauge.data.mock

import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.domain.models.Cohort

class MockCohortDataSource(private val users: List<User>) {

    private val cohorts = listOf(
        Cohort(
            id = "cohort_001",
            name = "Android Beginners",
            programme = "Mobile Development",
            startDate = "2026-04-01",
            geeks = users.filter { it.cohortId == "cohort_001" }
        ),
        Cohort(
            id = "cohort_002",
            name = "Web Fundamentals",
            programme = "Frontend Development",
            startDate = "2026-04-01",
            geeks = users.filter { it.cohortId == "cohort_002" }
        )
    )

    fun getAllCohorts(): List<Cohort> = cohorts

    fun getCohortById(id: String): Cohort? {
        return cohorts.firstOrNull { it.id == id }
    }
}

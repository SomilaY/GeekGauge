package com.somila.geekgauge.data.mock

import com.somila.geekgauge.domain.models.Cohort
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockCohortDataSource @Inject constructor(
    private val authDataSource: MockAuthDataSource  // gets users from auth source
) {
    private val cohorts: List<Cohort> by lazy {
        val users = authDataSource.getAllUsers()
        listOf(
            Cohort(
                id = "cohort_001",
                name = "Android Beginners",
                programme = "Mobile Development",
                startDate = "2026-04-01",
                endDate = "2026-11-01",
                geeks = users.filter { it.cohortId == "cohort_001" }
            ),
            Cohort(
                id = "cohort_002",
                name = "Web Fundamentals",
                programme = "Frontend Development",
                startDate = "2026-03-01",
                endDate = "2026-12-01",
                geeks = users.filter { it.cohortId == "cohort_002" }
            )
        )
    }

    fun getAllCohorts(): List<Cohort> = cohorts

    fun getCohortById(id: String): Cohort? = cohorts.firstOrNull { it.id == id }

    fun getGeekById(geekId: String) = authDataSource.getUserById(geekId)

    fun searchCohorts(query: String): List<Cohort> {
        if (query.isBlank()) return cohorts
        return cohorts.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.programme.contains(query, ignoreCase = true)
        }
    }

    fun searchGeeks(query: String) = authDataSource.getGeeks().filter {
        it.firstName.contains(query, ignoreCase = true) ||
                it.lastName.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true)
    }
}
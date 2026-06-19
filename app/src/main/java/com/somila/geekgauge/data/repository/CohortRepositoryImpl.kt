package com.somila.geekgauge.data.repository

import com.somila.geekgauge.data.mock.MockCohortDataSource
import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.domain.repository.CohortRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CohortRepositoryImpl @Inject constructor(
    private val mockCohortDataSource: MockCohortDataSource
) : CohortRepository {

    override fun getAllCohorts(): List<Cohort> =
        mockCohortDataSource.getAllCohorts()

    override fun getCohortById(id: String): Cohort? =
        mockCohortDataSource.getCohortById(id)

    override fun searchCohorts(query: String): List<Cohort> =
        mockCohortDataSource.searchCohorts(query)

    override fun searchGeeks(query: String): List<User> =
        mockCohortDataSource.searchGeeks(query)

    override fun getGeekById(geekId: String): User? =
        mockCohortDataSource.getGeekById(geekId)
}
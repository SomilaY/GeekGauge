package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.domain.models.User

interface CohortRepository {
    fun getAllCohorts(): List<Cohort>
    fun getCohortById(id: String): Cohort?
    fun searchCohorts(query: String): List<Cohort>
    fun searchGeeks(query: String): List<User>
    fun getGeekById(geekId: String): User?
}
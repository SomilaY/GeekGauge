package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.models.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun saveReport(report: Report): Result<Report>
    suspend fun getReportBySessionId(sessionId: String): Report?
    fun getReportsByGeekId(geekId: String): Flow<List<Report>>
}
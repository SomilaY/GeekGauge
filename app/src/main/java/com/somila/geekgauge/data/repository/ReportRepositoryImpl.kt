package com.somila.geekgauge.data.repository

import com.somila.geekgauge.data.local.dao.ReportDao
import com.somila.geekgauge.data.local.entities.toDomain
import com.somila.geekgauge.data.local.entities.toEntity
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao
) : ReportRepository {

    override suspend fun saveReport(report: Report): Result<Report> {
        return try {
            reportDao.insertReport(report.toEntity())
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportBySessionId(sessionId: String): Report? {
        return reportDao.getReportBySessionId(sessionId)?.toDomain()
    }

    override fun getReportsByGeekId(geekId: String): Flow<List<Report>> {
        return reportDao.getReportsByGeekId(geekId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
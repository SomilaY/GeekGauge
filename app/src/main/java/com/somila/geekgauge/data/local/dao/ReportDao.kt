package com.somila.geekgauge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.somila.geekgauge.data.local.entities.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getReportBySessionId(sessionId: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE geekId = :geekId ORDER BY createdAt DESC")
    fun getReportsByGeekId(geekId: String): Flow<List<ReportEntity>>
}
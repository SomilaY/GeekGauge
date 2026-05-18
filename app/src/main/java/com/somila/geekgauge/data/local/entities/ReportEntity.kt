package com.somila.geekgauge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val summary: String,
    val topics: String,          // store as JSON string
    val feedback: String,
    val recommendations: String  // store as JSON string
)
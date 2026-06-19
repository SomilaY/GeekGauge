package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.SyncStatus

data class Report(
    val id: String,
    val sessionId: String,
    val geekId: String,
    val summary: String,
    val feedback: String,
    val topics: List<Topic>,
    val recommendations: List<Recommendation>,
    val manualNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
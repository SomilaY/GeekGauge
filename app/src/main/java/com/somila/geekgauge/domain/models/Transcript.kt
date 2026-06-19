package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.SyncStatus

data class Transcript(
    val id: String,
    val sessionId: String,
    val rawText: String,
    val editedText: String = rawText,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
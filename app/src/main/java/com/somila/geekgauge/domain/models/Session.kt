package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.SessionType
import com.somila.geekgauge.domain.enums.SessionStatus
import com.somila.geekgauge.domain.enums.SyncStatus
data class Session(
    val id: String,
    val geekId: String,
    val trainerId: String,
    val type: SessionType,
    val status: SessionStatus,
    val audioFilePath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
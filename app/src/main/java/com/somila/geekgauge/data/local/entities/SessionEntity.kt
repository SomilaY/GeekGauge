package com.somila.geekgauge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.somila.geekgauge.domain.enums.SessionStatus
import com.somila.geekgauge.domain.enums.SessionType
import com.somila.geekgauge.domain.enums.SyncStatus
import com.somila.geekgauge.domain.models.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val geekId: String,
    val trainerId: String,
    val type: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val audioFilePath: String?,
    val syncStatus: String
)

fun SessionEntity.toDomain(): Session = Session(
    id = id,
    geekId = geekId,
    trainerId = trainerId,
    type = SessionType.valueOf(type),
    status = SessionStatus.valueOf(status),
    startTime = startTime,
    endTime = endTime,
    audioFilePath = audioFilePath,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    geekId = geekId,
    trainerId = trainerId,
    type = type.name,
    status = status.name,
    startTime = startTime,
    endTime = endTime,
    audioFilePath = audioFilePath,
    syncStatus = syncStatus.name
)
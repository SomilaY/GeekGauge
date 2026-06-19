package com.somila.geekgauge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.somila.geekgauge.domain.enums.SyncStatus
import com.somila.geekgauge.domain.models.Transcript

@Entity(tableName = "transcripts")
data class TranscriptEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val rawText: String,
    val editedText: String,
    val createdAt: Long,
    val syncStatus: String
)

fun TranscriptEntity.toDomain(): Transcript = Transcript(
    id = id,
    sessionId = sessionId,
    rawText = rawText,
    editedText = editedText,
    createdAt = createdAt,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Transcript.toEntity(): TranscriptEntity = TranscriptEntity(
    id = id,
    sessionId = sessionId,
    rawText = rawText,
    editedText = editedText,
    createdAt = createdAt,
    syncStatus = syncStatus.name
)
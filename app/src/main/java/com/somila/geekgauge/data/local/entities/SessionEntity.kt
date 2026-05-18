package com.somila.geekgauge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val geekId: String,
    val trainerId: String,
    val type: String,
    val status: String,
    val audioFilePath: String?,
    val syncStatus: String
)
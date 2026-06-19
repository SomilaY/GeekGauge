package com.somila.geekgauge.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.somila.geekgauge.data.local.entities.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscript(transcript: TranscriptEntity)

    @Update
    suspend fun updateTranscript(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getTranscriptBySessionId(sessionId: String): TranscriptEntity?
}
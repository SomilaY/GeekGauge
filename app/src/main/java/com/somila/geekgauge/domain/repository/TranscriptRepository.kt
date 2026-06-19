package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.domain.models.Transcript

interface TranscriptRepository {
    suspend fun saveTranscript(transcript: Transcript): Result<Transcript>
    suspend fun updateTranscript(transcript: Transcript): Result<Transcript>
    suspend fun getTranscriptBySessionId(sessionId: String): Transcript?
}
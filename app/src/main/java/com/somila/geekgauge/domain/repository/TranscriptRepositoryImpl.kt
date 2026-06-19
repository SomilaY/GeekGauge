package com.somila.geekgauge.domain.repository

import com.somila.geekgauge.data.local.dao.TranscriptDao
import com.somila.geekgauge.data.local.entities.toDomain
import com.somila.geekgauge.data.local.entities.toEntity
import com.somila.geekgauge.domain.models.Transcript
import com.somila.geekgauge.domain.repository.TranscriptRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptRepositoryImpl @Inject constructor(
    private val transcriptDao: TranscriptDao
) : TranscriptRepository {

    override suspend fun saveTranscript(transcript: Transcript): Result<Transcript> {
        return try {
            transcriptDao.insertTranscript(transcript.toEntity())
            Result.success(transcript)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTranscript(transcript: Transcript): Result<Transcript> {
        return try {
            transcriptDao.updateTranscript(transcript.toEntity())
            Result.success(transcript)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTranscriptBySessionId(sessionId: String): Transcript? {
        return transcriptDao.getTranscriptBySessionId(sessionId)?.toDomain()
    }
    
}
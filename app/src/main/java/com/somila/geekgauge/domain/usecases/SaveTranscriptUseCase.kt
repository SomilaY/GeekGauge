package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.domain.enums.SyncStatus
import com.somila.geekgauge.domain.models.Transcript
import com.somila.geekgauge.domain.repository.SessionRepository
import com.somila.geekgauge.domain.repository.TranscriptRepository
import java.util.UUID
import javax.inject.Inject

class SaveTranscriptUseCase @Inject constructor(
    private val transcriptRepository: TranscriptRepository,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        rawText: String
    ): Result<Transcript> {

        if (sessionId.isBlank()) {
            return Result.failure(TranscriptException.InvalidSession("Session ID cannot be blank"))
        }
        if (rawText.isBlank()) {
            return Result.failure(TranscriptException.EmptyTranscript("Transcript cannot be empty"))
        }

        sessionRepository.getSessionById(sessionId)
            ?: return Result.failure(TranscriptException.InvalidSession("Session $sessionId not found"))

        val existing = transcriptRepository.getTranscriptBySessionId(sessionId)

        return if (existing != null) {
            val updated = existing.copy(
                rawText = rawText,
                editedText = rawText
            )
            transcriptRepository.updateTranscript(updated)
        } else {
            val transcript = Transcript(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                rawText = rawText,
                editedText = rawText,
                syncStatus = SyncStatus.PENDING
            )
            transcriptRepository.saveTranscript(transcript)
        }
    }
}

sealed class TranscriptException(message: String) : Exception(message) {
    class InvalidSession(message: String) : TranscriptException(message)
    class EmptyTranscript(message: String) : TranscriptException(message)
    class SaveFailed(message: String) : TranscriptException(message)
}
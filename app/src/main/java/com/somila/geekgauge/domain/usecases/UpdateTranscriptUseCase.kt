package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.domain.models.Transcript
import com.somila.geekgauge.domain.repository.TranscriptRepository
import javax.inject.Inject

class UpdateTranscriptUseCase @Inject constructor(
    private val transcriptRepository: TranscriptRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        editedText: String
    ): Result<Transcript> {

        if (sessionId.isBlank()) {
            return Result.failure(
                TranscriptException.InvalidSession("Session ID cannot be blank")
            )
        }
        if (editedText.isBlank()) {
            return Result.failure(
                TranscriptException.EmptyTranscript("Edited transcript cannot be empty")
            )
        }

        val existing = transcriptRepository.getTranscriptBySessionId(sessionId)
            ?: return Result.failure(
                TranscriptException.InvalidSession("No transcript found for session $sessionId")
            )

        val updated = existing.copy(editedText = editedText)
        return transcriptRepository.updateTranscript(updated)
    }
}
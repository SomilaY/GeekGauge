package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.domain.models.Transcript
import com.somila.geekgauge.domain.repository.TranscriptRepository
import javax.inject.Inject

class GetTranscriptUseCase @Inject constructor(
    private val transcriptRepository: TranscriptRepository
) {
    suspend operator fun invoke(sessionId: String): Result<Transcript> {
        if (sessionId.isBlank()) {
            return Result.failure(
                TranscriptException.InvalidSession("Session ID cannot be blank")
            )
        }

        val transcript = transcriptRepository.getTranscriptBySessionId(sessionId)
            ?: return Result.failure(
                TranscriptException.InvalidSession("No transcript found for session $sessionId")
            )

        return Result.success(transcript)
    }
}
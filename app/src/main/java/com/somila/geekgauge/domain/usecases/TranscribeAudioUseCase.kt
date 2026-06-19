package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.domain.TranscriptionService
import javax.inject.Inject

class TranscribeAudioUseCase @Inject constructor(
    private val transcriptionService: TranscriptionService
) {
    suspend operator fun invoke(): Result<String> {
        if (!transcriptionService.isAvailable()) {
            return Result.failure(
                IllegalStateException("Transcription service is not available on this device")
            )
        }
        return transcriptionService.transcribeAudio()
    }
}
package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.data.gemini.GeminiRepository
import com.somila.geekgauge.data.gemini.GeminiResponseParser
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.repository.ReportRepository
import com.somila.geekgauge.domain.repository.SessionRepository
import com.somila.geekgauge.domain.repository.TranscriptRepository
import javax.inject.Inject

class GenerateReportUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val geminiResponseParser: GeminiResponseParser,
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository,
    private val transcriptRepository: TranscriptRepository
) {
    suspend operator fun invoke(sessionId: String, geekName: String): Result<Report> {

        // 1. Load session
        val session = sessionRepository.getSessionById(sessionId)
            ?: return Result.failure(IllegalStateException("Session $sessionId not found"))

        // 2. Load transcript
        val transcript = transcriptRepository.getTranscriptBySessionId(sessionId)
            ?: return Result.failure(IllegalStateException("No transcript found for session $sessionId"))

        // 3. Call Gemini with edited transcript
        val geminiResult = geminiRepository.generateReport(
            transcript = transcript.editedText,
            sessionType = session.type.name,
            geekName = geekName
        )

        if (geminiResult.isFailure) {
            return Result.failure(geminiResult.exceptionOrNull()
                ?: IllegalStateException("Gemini call failed"))
        }

        // 4. Parse JSON response into Report domain model
        val jsonString = geminiResult.getOrThrow()
        val parseResult = geminiResponseParser.parse(
            jsonString = jsonString,
            sessionId = sessionId,
            geekId = session.geekId
        )

        if (parseResult.isFailure) {
            return Result.failure(parseResult.exceptionOrNull()
                ?: IllegalStateException("Failed to parse report"))
        }

        // 5. Save report to Room
        val report = parseResult.getOrThrow()
        return reportRepository.saveReport(report)
    }
}
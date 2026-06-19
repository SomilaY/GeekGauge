package com.somila.geekgauge.data.gemini

import com.somila.geekgauge.BuildConfig
import com.somila.geekgauge.domain.GeminiPromptBuilder.buildSystemInstruction
import com.somila.geekgauge.domain.GeminiPromptBuilder.buildUserContent
import com.somila.geekgauge.data.remote.GeminiApiService
import com.somila.geekgauge.domain.GeminiPromptBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val geminiApiService: GeminiApiService
) {
    suspend fun generateReport(
        transcript: String,
        sessionType: String,
        geekName: String
    ): Result<String> {
        return try {
            val request = GeminiRequest(
                contents = listOf(GeminiPromptBuilder.buildUserContent(transcript)),
                systemInstruction = GeminiPromptBuilder.buildSystemInstruction(sessionType, geekName),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json"
                )
            )

            // Make sure your GeminiApiService is updated to use "gemini-2.5-flash"
            val response = geminiApiService.generateReport(
                modelName = "gemini-2.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            val rawText = response
                .candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (rawText.isNullOrBlank()) {
                Result.failure(GeminiException.EmptyResponse)
            } else {

                val cleanJson = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                Result.success(cleanJson)
            }
        } catch (e: Exception) {
            Result.failure(GeminiException.NetworkError(e.message ?: "Unknown error"))
        }
    }
}

sealed class GeminiException(message: String) : Exception(message) {
    object EmptyResponse : GeminiException("Gemini returned empty response")
    data class NetworkError(val error: String) : GeminiException(error)
    data class ParseError(val error: String) : GeminiException(error)
}
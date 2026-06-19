package com.somila.geekgauge.data.gemini

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,

    // v1 expects snake_case here
    @SerialName("system_instruction")
    val systemInstruction: SystemInstruction? = null,

    // v1 expects snake_case here
    @SerialName("generation_config")
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val role: String = "user",
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class SystemInstruction(
    val parts: List<Part>
)

@Serializable
data class GenerationConfig(
    // v1 expects snake_case here
    @SerialName("response_mime_type")
    val responseMimeType: String = "application/json",
    val temperature: Double = 0.7
)
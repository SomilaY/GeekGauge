package com.somila.geekgauge.domain

interface TranscriptionService {
    suspend fun transcribeAudio(): Result<String>
    fun isAvailable(): Boolean
}
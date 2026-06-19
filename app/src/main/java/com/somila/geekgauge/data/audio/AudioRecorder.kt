package com.somila.geekgauge.data.audio

import java.io.File

interface AudioRecorder {
    suspend fun startRecording(outputFile: File): Result<Unit>
    suspend fun pauseRecording(): Result<Unit>
    suspend fun resumeRecording(): Result<Unit>
    suspend fun stopRecording(): Result<File>
    fun getCurrentAmplitude(): Int
    fun getRecordingDurationMs(): Long
    val isRecording: Boolean
    val isPaused: Boolean
    fun cleanup()
}
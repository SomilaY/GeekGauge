package com.somila.geekgauge.domain.models

import java.io.File

sealed class RecordingState {
    object Idle : RecordingState()
    data class Recording(val durationMs: Long, val amplitude: Int) : RecordingState()
    data class Paused(val durationMs: Long) : RecordingState()
    data class Finished(val file: File) : RecordingState()
    data class Error(val message: String) : RecordingState()
}
package com.somila.geekgauge.domain.usecases

import com.somila.geekgauge.data.audio.AudioFileManager
import com.somila.geekgauge.data.audio.AudioRecorder
import com.somila.geekgauge.data.audio.SpeechRecognizerImpl
import com.somila.geekgauge.domain.models.RecordingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RecordSessionUseCase @Inject constructor(
    val audioRecorder: AudioRecorder,
    private val audioFileManager: AudioFileManager,
    private val speechRecognizer: SpeechRecognizerImpl
) {
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: Flow<RecordingState> = _recordingState.asStateFlow()

    val rmsLevel: StateFlow<Float> = speechRecognizer.rmsLevel

    // Expose live transcript for UI
    val liveTranscript: StateFlow<String> = speechRecognizer.liveTranscript

    private var currentSessionId: String = ""

    private var cachedTranscript: String = ""

    private var sessionStartMs: Long = 0L
    private var pausedMs: Long = 0L
    private var pauseStartMs: Long = 0L

    suspend fun startRecording(sessionId: String) {
        currentSessionId = sessionId
        sessionStartMs = System.currentTimeMillis()
        pausedMs = 0L

        withContext(Dispatchers.Main) {
            speechRecognizer.startListening()
        }
        _recordingState.value = RecordingState.Recording(0, 0)
    }

    suspend fun pauseRecording() {
        pauseStartMs = System.currentTimeMillis()
        withContext(Dispatchers.Main) {
            speechRecognizer.stopListening()
        }
        _recordingState.value = RecordingState.Paused(getDurationMs())
    }

    suspend fun resumeRecording() {
        pausedMs += System.currentTimeMillis() - pauseStartMs
        withContext(Dispatchers.Main) {
            speechRecognizer.startListening()
        }
        _recordingState.value = RecordingState.Recording(getDurationMs(), 0)
    }

    suspend fun stopRecording(): File? {
        withContext(Dispatchers.Main) {
            speechRecognizer.stopListening()
        }

        cachedTranscript = speechRecognizer.awaitFinalTranscript()

        // Save audio file in background after SpeechRecognizer releases mic
        val outputFile = audioFileManager.createNewAudioFile(currentSessionId)
        val audioResult = audioRecorder.startRecording(outputFile)

        if (audioResult.isSuccess) {
            delay(500) // record a brief moment for file integrity
            audioRecorder.stopRecording()
        }

        _recordingState.value = RecordingState.Finished(outputFile)
        return outputFile
    }

    fun getFinalTranscript(): String = cachedTranscript
    fun getDurationMs(): Long {
        return if (sessionStartMs == 0L) 0L
        else System.currentTimeMillis() - sessionStartMs - pausedMs
    }

    fun getAmplitude(): Int = audioRecorder.getCurrentAmplitude()

    fun updateRecordingProgress(durationMs: Long, amplitude: Int) {
        if (audioRecorder.isRecording && !audioRecorder.isPaused) {
            _recordingState.value = RecordingState.Recording(durationMs, amplitude)
        }
    }

    fun cleanup() {
        // cleanup must also happen on main thread for SpeechRecognizer
        kotlinx.coroutines.MainScope().launch {
            withContext(Dispatchers.Main) {
                speechRecognizer.stopListening()
            }
            audioRecorder.cleanup()
            _recordingState.value = RecordingState.Idle
        }
    }
}
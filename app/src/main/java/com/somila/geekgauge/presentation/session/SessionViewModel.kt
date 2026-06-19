package com.somila.geekgauge.presentation.session

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somila.geekgauge.core.NotificationHelper
import com.somila.geekgauge.core.ServiceManager
import com.somila.geekgauge.domain.enums.SessionType
import com.somila.geekgauge.domain.models.RecordingState
import com.somila.geekgauge.domain.usecases.RecordSessionUseCase
import com.somila.geekgauge.domain.usecases.SaveTranscriptUseCase
import com.somila.geekgauge.domain.usecases.StartSessionUseCase
import com.somila.geekgauge.domain.usecases.TranscribeAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val recordSessionUseCase: RecordSessionUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val saveTranscriptUseCase: SaveTranscriptUseCase,
    private val serviceManager: ServiceManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _sessionState = MutableStateFlow<SessionUiState>(SessionUiState.Idle)
    val sessionState: StateFlow<SessionUiState> = _sessionState.asStateFlow()

    val rmsLevel: StateFlow<Float> = recordSessionUseCase.rmsLevel

    val liveTranscript: StateFlow<String> = recordSessionUseCase.liveTranscript

    private var progressJob: Job? = null
    private var currentSessionId: String? = null

    init {
        observeRecordingState()
    }

    // Collect recording state once, for the lifetime of the ViewModel
    private fun observeRecordingState() {
        viewModelScope.launch {
            recordSessionUseCase.recordingState.collect { state ->
                when (state) {
                    is RecordingState.Recording -> {
                        _sessionState.value = SessionUiState.Recording(
                            durationMs = state.durationMs,
                            amplitude = state.amplitude
                        )
                    }
                    is RecordingState.Paused -> {
                        _sessionState.value = SessionUiState.Paused(state.durationMs)
                        stopProgressUpdates()
                    }
                    is RecordingState.Finished -> {
                        stopProgressUpdates()
                        _sessionState.value = SessionUiState.Processing
                        processRecording(state.file.path)
                    }
                    is RecordingState.Error -> {
                        stopProgressUpdates()
                        _sessionState.value = SessionUiState.Error(state.message)
                    }
                    is RecordingState.Idle -> {
                        // no-op, handled by startSession
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startSession(geekId: String, trainerId: String, sessionType: SessionType) {
        viewModelScope.launch {
            _sessionState.value = SessionUiState.Preparing

            startSessionUseCase(geekId, trainerId, sessionType)
                .onSuccess { session ->
                    currentSessionId = session.id
                    serviceManager.startRecordingService() // ← start foreground service
                    recordSessionUseCase.startRecording(session.id)
                    startProgressUpdates()
                }
                .onFailure { error ->
                    _sessionState.value = SessionUiState.Error(
                        error.message ?: "Failed to start session"
                    )
                }
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            recordSessionUseCase.pauseRecording()
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            recordSessionUseCase.resumeRecording()
            startProgressUpdates()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            stopProgressUpdates()
            serviceManager.stopRecordingService() // ← stop foreground service
            recordSessionUseCase.stopRecording()
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            delay(300)
            while (isActive) {
                delay(100)
                val duration = recordSessionUseCase.getDurationMs()
                val current = _sessionState.value
                if (current is SessionUiState.Recording) {
                    _sessionState.value = current.copy(durationMs = duration)
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun processRecording(audioFilePath: String) {
        viewModelScope.launch {
            val sessionId = currentSessionId ?: run {
                _sessionState.value = SessionUiState.Error("No active session")
                return@launch
            }

            val transcript = recordSessionUseCase.getFinalTranscript()

            if (transcript.isBlank()) {
                _sessionState.value = SessionUiState.Error(
                    "No speech was captured during the session"
                )
                return@launch
            }

            saveTranscriptUseCase(sessionId, transcript)
                .onSuccess {
                    // Fire report ready notification
                    notificationHelper.showReportReadyNotification(
                        geekName = "Learner", // replace with actual name later
                        sessionId = sessionId
                    )
                    _sessionState.value = SessionUiState.TranscriptReady(sessionId)
                }
                .onFailure { error ->
                    _sessionState.value = SessionUiState.Error(
                        error.message ?: "Failed to save transcript"
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        serviceManager.stopRecordingService() // safety net if ViewModel is cleared
        recordSessionUseCase.cleanup()
    }
}

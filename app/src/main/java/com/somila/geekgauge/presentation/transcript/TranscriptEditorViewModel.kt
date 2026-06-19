package com.somila.geekgauge.presentation.transcript

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somila.geekgauge.domain.usecases.GenerateReportUseCase
import com.somila.geekgauge.domain.usecases.GetTranscriptUseCase
import com.somila.geekgauge.domain.usecases.UpdateTranscriptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranscriptEditorViewModel @Inject constructor(
    private val getTranscriptUseCase: GetTranscriptUseCase,
    private val updateTranscriptUseCase: UpdateTranscriptUseCase,
    private val generateReportUseCase: GenerateReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TranscriptEditorUiState>(
        TranscriptEditorUiState.Loading
    )
    val uiState: StateFlow<TranscriptEditorUiState> = _uiState.asStateFlow()

    private val _reportGenerationState = MutableStateFlow<ReportGenerationState>(
        ReportGenerationState.Idle
    )
    val reportGenerationState: StateFlow<ReportGenerationState> =
        _reportGenerationState.asStateFlow()

    fun loadTranscript(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = TranscriptEditorUiState.Loading

            getTranscriptUseCase(sessionId)
                .onSuccess { transcript ->
                    _uiState.value = TranscriptEditorUiState.Editing(
                        sessionId = sessionId,
                        rawText = transcript.rawText,
                        editedText = transcript.editedText
                    )
                }
                .onFailure { error ->
                    _uiState.value = TranscriptEditorUiState.Error(
                        error.message ?: "Failed to load transcript"
                    )
                }
        }
    }

    // Called every time the trainer types a character
    fun onEditedTextChanged(newText: String) {
        val current = _uiState.value
        if (current is TranscriptEditorUiState.Editing) {
            _uiState.value = current.copy(
                editedText = newText,
                hasUnsavedChanges = newText != current.rawText
            )
        }
    }

    // Save edits to Room
    fun saveTranscript() {
        val current = _uiState.value
        if (current !is TranscriptEditorUiState.Editing) return

        viewModelScope.launch {
            updateTranscriptUseCase(current.sessionId, current.editedText)
                .onSuccess {
                    _uiState.value = current.copy(hasUnsavedChanges = false)
                }
                .onFailure { error ->
                    _uiState.value = TranscriptEditorUiState.Error(
                        error.message ?: "Failed to save transcript"
                    )
                }
        }
    }

    // Save then trigger Gemini report generation
    fun generateReport(geekName: String) {
        val current = _uiState.value
        if (current !is TranscriptEditorUiState.Editing) return

        viewModelScope.launch {
            _reportGenerationState.value = ReportGenerationState.Loading

            // Save latest edits first
            updateTranscriptUseCase(current.sessionId, current.editedText)
                .onFailure { error ->
                    _reportGenerationState.value = ReportGenerationState.Error(
                        error.message ?: "Failed to save transcript before generating report"
                    )
                    return@launch
                }

            // Then generate report
            generateReportUseCase(current.sessionId, geekName)
                .onSuccess { report ->
                    _reportGenerationState.value = ReportGenerationState.Success(report.id)
                }
                .onFailure { error ->
                    _reportGenerationState.value = ReportGenerationState.Error(
                        error.message ?: "Failed to generate report"
                    )
                }
        }
    }

    // Reset report generation state after navigating away
    fun resetReportState() {
        _reportGenerationState.value = ReportGenerationState.Idle
    }
}

sealed class ReportGenerationState {
    object Idle : ReportGenerationState()
    object Loading : ReportGenerationState()
    data class Success(val reportId: String) : ReportGenerationState()
    data class Error(val message: String) : ReportGenerationState()
}
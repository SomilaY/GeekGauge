package com.somila.geekgauge.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somila.geekgauge.domain.repository.ReportRepository
import com.somila.geekgauge.domain.usecases.ExportReportPdfUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val exportReportPdfUseCase: ExportReportPdfUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun loadReport(sessionId: String) {
        viewModelScope.launch {
            val report = reportRepository.getReportBySessionId(sessionId)
            if (report != null) {
                _uiState.value = ReportUiState.Success(report)
            } else {
                _uiState.value = ReportUiState.Error("Report not found")
            }
        }
    }

    fun exportPdf(sessionId: String) {
        val current = _uiState.value
        if (current !is ReportUiState.Success) return

        viewModelScope.launch {
            _exportState.value = ExportState.Exporting

            exportReportPdfUseCase(current.report.id, sessionId)
                .onSuccess { file ->
                    _exportState.value = ExportState.Success(file)
                }
                .onFailure { error ->
                    _exportState.value = ExportState.Error(
                        error.message ?: "Failed to export PDF"
                    )
                }
        }
    }

    fun saveManualNotes(sessionId: String, notes: String) {
        val current = _uiState.value
        if (current !is ReportUiState.Success) return

        viewModelScope.launch {
            val updated = current.report.copy(manualNotes = notes)
            reportRepository.saveReport(updated)
            _uiState.value = ReportUiState.Success(updated)
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
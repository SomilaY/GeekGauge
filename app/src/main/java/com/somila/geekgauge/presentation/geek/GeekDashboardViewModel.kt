package com.somila.geekgauge.presentation.geek

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.models.Session
import com.somila.geekgauge.domain.repository.ReportRepository
import com.somila.geekgauge.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeekDashboardViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GeekDashboardUiState>(GeekDashboardUiState.Loading)
    val uiState: StateFlow<GeekDashboardUiState> = _uiState.asStateFlow()

    // Hardcoded for now — replace with auth state holder later
    private val currentGeekId = "g001"

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            reportRepository.getReportsByGeekId(currentGeekId)
                .catch { error ->
                    _uiState.value = GeekDashboardUiState.Error(
                        error.message ?: "Failed to load reports"
                    )
                }
                .collect { reports ->
                    _uiState.value = GeekDashboardUiState.Success(
                        geekName = "Thandeka Nkosi",
                        reports = reports.sortedByDescending { it.createdAt }
                    )
                }
        }
    }
}

sealed class GeekDashboardUiState {
    object Loading : GeekDashboardUiState()
    data class Success(
        val geekName: String,
        val reports: List<Report>
    ) : GeekDashboardUiState()
    data class Error(val message: String) : GeekDashboardUiState()
}
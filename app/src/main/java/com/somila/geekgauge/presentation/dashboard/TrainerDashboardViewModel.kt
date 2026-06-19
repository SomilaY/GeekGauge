package com.somila.geekgauge.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.somila.geekgauge.data.mock.MockCohortDataSource
import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.domain.repository.CohortRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class TrainerDashboardViewModel @Inject constructor(
    private val cohortRepository: CohortRepository
) : ViewModel() {

    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())
    val cohorts: StateFlow<List<Cohort>> = _cohorts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCohorts()
    }

    private fun loadCohorts() {
        _cohorts.value = cohortRepository.getAllCohorts()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _cohorts.value = cohortRepository.searchCohorts(query)
    }
}
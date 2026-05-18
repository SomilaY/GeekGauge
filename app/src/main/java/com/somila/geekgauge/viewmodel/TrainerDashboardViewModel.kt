package com.somila.geekgauge.viewmodel

import androidx.lifecycle.ViewModel
import com.somila.geekgauge.data.local.GeekGaugeDatabase
import com.somila.geekgauge.data.mock.MockCohortDataSource
import com.somila.geekgauge.domain.models.Cohort
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class TrainerDashboardViewModel @Inject constructor(
    private val cohortDataSource: MockCohortDataSource
) : ViewModel() {

    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())
    val cohorts: StateFlow<List<Cohort>> = _cohorts

    init {
        loadCohorts()
    }

    private fun loadCohorts() {
        _cohorts.value = cohortDataSource.getAllCohorts()
    }
}


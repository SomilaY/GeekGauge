package com.somila.geekgauge.presentation.geek

import androidx.lifecycle.ViewModel
import com.somila.geekgauge.data.mock.MockCohortDataSource
import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.domain.repository.CohortRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class GeekDetailViewModel @Inject constructor(
    private val cohortRepository: CohortRepository
) : ViewModel() {

    private val _cohort = MutableStateFlow<Cohort?>(null)
    val cohort: StateFlow<Cohort?> = _cohort.asStateFlow()

    private val _filteredGeeks = MutableStateFlow<List<User>>(emptyList())
    val filteredGeeks: StateFlow<List<User>> = _filteredGeeks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun loadCohort(cohortId: String?) {
        val loaded = cohortRepository.getCohortById(cohortId ?: "")
        _cohort.value = loaded
        _filteredGeeks.value = loaded?.geeks ?: emptyList()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val allGeeks = _cohort.value?.geeks ?: emptyList()
        _filteredGeeks.value = if (query.isBlank()) {
            allGeeks
        } else {
            allGeeks.filter {
                it.firstName.contains(query, ignoreCase = true) ||
                        it.lastName.contains(query, ignoreCase = true) ||
                        it.email.contains(query, ignoreCase = true)
            }
        }
    }
}
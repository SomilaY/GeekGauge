package com.somila.geekgauge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.mock.MockAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authDataSource: MockAuthDataSource
) : ViewModel() {

    private val _loginState = MutableStateFlow<User?>(null)
    val loginState: StateFlow<User?> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = authDataSource.login(email, password)
            _loginState.value = user
        }
    }
}
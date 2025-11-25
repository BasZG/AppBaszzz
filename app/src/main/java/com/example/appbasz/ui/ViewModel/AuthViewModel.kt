package com.example.appbasz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.model.User
import com.example.appbasz.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        checkCurrentUser()
        setupAuthListener()
    }

    private fun checkCurrentUser() {
        val user = authRepository.getCurrentUser()
        _currentUser.value = user
        _isAuthenticated.value = user != null
    }

    private fun setupAuthListener() {
        viewModelScope.launch {
            authRepository.getAuthState().collect { isAuthenticated ->
                _isAuthenticated.value = isAuthenticated
                if (isAuthenticated) {
                    _currentUser.value = authRepository.getCurrentUser()
                    _authError.value = null
                } else {
                    _currentUser.value = null
                }
            }
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _isLoading.value = false

            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _authError.value = null
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        _isLoading.value = true
        _authError.value = null

        viewModelScope.launch {
            val result = authRepository.register(email, password, displayName)
            _isLoading.value = false

            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _authError.value = null
            } else {
                _authError.value = getErrorMessage(result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _isAuthenticated.value = false
        }
    }

    fun clearError() {
        _authError.value = null
    }

    private fun getErrorMessage(error: String?): String {
        return when {
            error == null -> "Error desconocido"
            error.contains("email address is badly formatted") -> "Formato de email inválido"
            error.contains("password is invalid") -> "Contraseña incorrecta"
            error.contains("email address is already in use") -> "El email ya está en uso"
            error.contains("password must be at least 6 characters") -> "La contraseña debe tener al menos 6 caracteres"
            error.contains("network error") -> "Error de conexión. Verifica tu internet"
            else -> error
        }
    }
}
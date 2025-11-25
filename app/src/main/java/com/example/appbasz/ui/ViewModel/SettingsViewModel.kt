package com.example.appbasz.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.local.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    val isDarkThemeEnabled = preferencesManager.getDarkThemeEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isUserLoggedIn = preferencesManager.getUserLoggedIn()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val areNotificationsEnabled = preferencesManager.getNotificationsEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkThemeEnabled(enabled)
        }
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        viewModelScope.launch {
            preferencesManager.setUserLoggedIn(loggedIn)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }
}
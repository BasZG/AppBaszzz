// ViewModel/ProfileViewModel.kt
package com.example.appbasz.ViewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.local.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfileImage()
    }

    fun setProfileImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Guardar la URI en PreferencesManager
                preferencesManager.saveProfileImageUri(uri.toString())
                _profileImageUri.value = uri
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            preferencesManager.removeProfileImage()
            _profileImageUri.value = null
        }
    }

    private fun loadProfileImage() {
        viewModelScope.launch {
            preferencesManager.getProfileImageUri().collect { uriString ->
                if (uriString.isNotEmpty()) {
                    _profileImageUri.value = Uri.parse(uriString)
                }
            }
        }
    }
}
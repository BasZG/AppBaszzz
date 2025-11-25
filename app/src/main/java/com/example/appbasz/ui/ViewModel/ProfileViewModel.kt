package com.example.appbasz.ViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.local.PreferencesManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val database = Firebase.database.reference
    private val currentUser = Firebase.auth.currentUser

    init {
        loadProfileImage()
    }

    fun setProfileImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = currentUser?.uid
                if (userId == null) {
                    return@launch
                }

                val base64Image = uriToBase64(uri, context)
                saveImageToFirebase(userId, base64Image)

                preferencesManager.saveProfileImageUri(uri.toString())
                _profileImageUri.value = uri

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveImageToFirebase(userId: String, base64Image: String) {
        val userData = mapOf(
            "profileImage" to base64Image,
            "email" to currentUser?.email,
            "displayName" to currentUser?.displayName
        )

        database.child("users").child(userId).setValue(userData).await()
    }

    private suspend fun uriToBase64(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

        if (inputStream == null) {
            throw Exception("No se pudo abrir la imagen")
        }

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (bitmap == null) {
            throw Exception("No se pudo decodificar la imagen")
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val imageBytes = outputStream.toByteArray()
        outputStream.close()

        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            val userId = currentUser?.uid
            if (userId != null) {
                try {
                    database.child("users").child(userId).child("profileImage").removeValue().await()
                } catch (e: Exception) {
                    // Error silencioso
                }
            }

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
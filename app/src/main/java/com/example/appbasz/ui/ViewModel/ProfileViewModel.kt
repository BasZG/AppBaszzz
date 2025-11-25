package com.example.appbasz.ViewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
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

    // MODIFICAR: Agregar parámetro de contexto
    fun setProfileImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("FIREBASE_DEBUG", "=== INICIANDO GUARDADO DE IMAGEN ===")
            Log.d("FIREBASE_DEBUG", "URI recibida: $uri")

            try {
                // 1. Verificar usuario
                val userId = currentUser?.uid
                Log.d("FIREBASE_DEBUG", "Usuario ID: $userId")

                if (userId == null) {
                    Log.e("FIREBASE_DEBUG", "ERROR: Usuario no autenticado")
                    return@launch
                }

                // 2. Convertir imagen a Base64 - PASAR CONTEXTO
                Log.d("FIREBASE_DEBUG", "Convirtiendo imagen a Base64...")
                val base64Image = uriToBase64(uri, context) // <- Contexto aquí
                Log.d("FIREBASE_DEBUG", "Base64 generado - Longitud: ${base64Image.length}")
                Log.d("FIREBASE_DEBUG", "Primeros 50 chars: ${base64Image.take(50)}...")

                // 3. Guardar en Firebase
                Log.d("FIREBASE_DEBUG", "Guardando en Firebase...")
                saveImageToFirebase(userId, base64Image)

                // 4. Guardar localmente
                Log.d("FIREBASE_DEBUG", "Guardando localmente...")
                preferencesManager.saveProfileImageUri(uri.toString())
                _profileImageUri.value = uri

                Log.d("FIREBASE_DEBUG", "=== IMAGEN GUARDADA EXITOSAMENTE ===")

            } catch (e: Exception) {
                Log.e("FIREBASE_DEBUG", "ERROR CRÍTICO: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun saveImageToFirebase(userId: String, base64Image: String) {
        try {
            Log.d("FIREBASE_DEBUG", "Conectando con Firebase Database...")

            // Crear estructura completa del usuario si no existe
            val userData = mapOf(
                "profileImage" to base64Image,
                "email" to currentUser?.email,
                "displayName" to currentUser?.displayName
            )

            Log.d("FIREBASE_DEBUG", "Guardando en: users/$userId")
            database.child("users").child(userId).setValue(userData).await()

            Log.d("FIREBASE_DEBUG", "Imagen guardada en Firebase exitosamente")

        } catch (e: Exception) {
            Log.e("FIREBASE_DEBUG", "Error Firebase: ${e.message}", e)
            throw e
        }
    }

    // MODIFICAR: Recibir contexto como parámetro
    private suspend fun uriToBase64(uri: Uri, context: Context): String {
        return try {
            Log.d("FIREBASE_DEBUG", "Abriendo InputStream del URI...")

            // USAR EL CONTEXTO RECIBIDO en lugar de ContextWrapper(null)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Log.e("FIREBASE_DEBUG", "No se pudo abrir el InputStream")
                throw Exception("No se pudo abrir la imagen")
            }

            Log.d("FIREBASE_DEBUG", "Decodificando bitmap...")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap == null) {
                Log.e("FIREBASE_DEBUG", "No se pudo decodificar el bitmap")
                throw Exception("No se pudo decodificar la imagen")
            }

            Log.d("FIREBASE_DEBUG", "Bitmap decodificado - Dimensiones: ${bitmap.width}x${bitmap.height}")

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val imageBytes = outputStream.toByteArray()
            outputStream.close()

            Log.d("FIREBASE_DEBUG", "Bytes de imagen: ${imageBytes.size} bytes")

            val base64 = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
            Log.d("FIREBASE_DEBUG", "Base64 generado correctamente")

            base64

        } catch (e: Exception) {
            Log.e("FIREBASE_DEBUG", "Error en conversión Base64: ${e.message}", e)
            throw e
        }
    }

    fun removeProfileImage() {
        viewModelScope.launch {
            val userId = currentUser?.uid
            if (userId != null) {
                try {
                    database.child("users").child(userId).child("profileImage").removeValue().await()
                    Log.d("FIREBASE_DEBUG", "Imagen eliminada de Firebase")
                } catch (e: Exception) {
                    Log.e("FIREBASE_DEBUG", "Error eliminando imagen: ${e.message}")
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
                    Log.d("FIREBASE_DEBUG", "Imagen local cargada: $uriString")
                }
            }
        }
    }
}
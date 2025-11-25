package com.example.appbasz.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit
): ImagePickerState {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { onImageSelected(it) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // La URI se maneja en el estado
            }
        }
    )

    return remember {
        ImagePickerState(
            galleryLauncher = galleryLauncher,
            cameraLauncher = cameraLauncher,
            onImageSelected = onImageSelected
        )
    }
}

class ImagePickerState(
    val galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    private val onImageSelected: (Uri) -> Unit
) {
    private var tempUri: Uri? = null

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    fun openCamera(uri: Uri) {
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    fun onCameraResult(success: Boolean) {
        if (success) {
            tempUri?.let { onImageSelected(it) }
        }
        tempUri = null
    }
}
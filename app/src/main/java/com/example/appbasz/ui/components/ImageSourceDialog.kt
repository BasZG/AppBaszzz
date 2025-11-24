// ui/components/ImageSourceDialog.kt
package com.example.appbasz.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar imagen") },
        text = { Text("¿De dónde quieres tomar la foto?") },
        confirmButton = {
            TextButton(onClick = onGallerySelected) {
                Text("Galería")
            }
        },
        dismissButton = {
            TextButton(onClick = onCameraSelected) {
                Text("Cámara")
            }
        }
    )
}
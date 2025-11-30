package com.example.reciclapp.screens.map

import android.net.Uri
import com.google.firebase.firestore.GeoPoint

data class RegisterUiState(
    // Datos del formulario
    val name: String = "",
    val description: String = "",
    val coordinates: GeoPoint? = null,

    // Estado de envío a Firestore
    val isSubmitting: Boolean = false,

    // Imagen seleccionada (galería / cámara)
    val selectedImageUri: Uri? = null,

    // Estado del análisis con Roboflow
    val isAnalyzingImage: Boolean = false,
    val imageAnalysisResult: String? = null,
    val imageAnalysisError: String? = null,

    // Resultado interpretado del modelo
    val isRecycleCenter: Boolean? = null,
    val modelMessage: String? = null
)
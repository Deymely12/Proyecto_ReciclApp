package com.example.reciclapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.data.marker.MarkerRepository
import com.example.reciclapp.data.trashbin.RoboflowRepository
import com.example.reciclapp.screens.map.RegisterUiState
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import com.example.reciclapp.data.marker.FirebaseMarkerRepository
import com.example.reciclapp.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.json.JSONArray
import org.json.JSONObject

class RegisterViewModel(
    private val markerRepository: MarkerRepository = FirebaseMarkerRepository(FirebaseFirestore.getInstance()),
    private val roboflowRepository: RoboflowRepository = RoboflowRepository()
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onNameChange(value: String) {
        uiState = uiState.copy(name = value)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value)
    }

    fun onCoordinatesChange(value: GeoPoint) {
        uiState = uiState.copy(coordinates = value)
    }

    fun onDireccionChange(value: String) {
        uiState = uiState.copy(direccion = value)
    }

    fun onImageSelected(uri: Uri?) {
        uiState = uiState.copy(
            selectedImageUri = uri,
            imageAnalysisResult = null,
            imageAnalysisError = null
        )
    }

    fun analyzeImage(context: Context) {
        val uri = uiState.selectedImageUri ?: return

        uiState = uiState.copy(
            isAnalyzingImage = true,
            imageAnalysisResult = null,
            imageAnalysisError = null,
            isRecycleCenter = null,
            modelMessage = null
        )

        viewModelScope.launch {
            try {
                val result = roboflowRepository.analyzeImage(context, uri)

                // parseo del JSON
                val isTrashBin = try {
                    val root = JSONObject(result)
                    val preds = root.optJSONArray("predictions") ?: JSONArray()
                    var found = false
                    for (i in 0 until preds.length()) {
                        val p = preds.getJSONObject(i)
                        val clazz = p.optString("class")  // campo "class" del modelo
                        if (clazz == "TrashBin") {
                            found = true
                            break
                        }
                    }
                    found
                } catch (e: Exception) {
                    false
                }

                val message = if (isTrashBin) {
                    "El modelo ha analizado la foto y detectó un centro de reciclaje"
                } else {
                    "El modelo no reconoció un centro de reciclaje, se envió la solicitud al administrador"
                }

                uiState = uiState.copy(
                    isAnalyzingImage = false,
                    imageAnalysisResult = result,
                    imageAnalysisError = null,
                    isRecycleCenter = isTrashBin,
                    modelMessage = message
                )

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isAnalyzingImage = false,
                    imageAnalysisError = e.message,
                    isRecycleCenter = null,
                    modelMessage = null
                )
            }
        }
    }
    fun submit(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val name = uiState.name
        val description = uiState.description
        val coordinates = uiState.coordinates
        val direccion = uiState.direccion

        if (name.isBlank() || description.isBlank() || coordinates == null || direccion.isBlank()) {
            onError("Por favor, completa todos los campos")
            return
        }

        uiState = uiState.copy(isSubmitting = true)

        // true si el modelo detectó TrashBin, false si no
        val isRecycleCenter = uiState.isRecycleCenter == true

        // Usamos el data class Marker
        val marker = Marker(
            id = "",
            name = name,
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            description = description,
            direccion = direccion,
            isEnabled = isRecycleCenter
        )

        viewModelScope.launch {
            try {
                markerRepository.registerMarker(marker)
                uiState = uiState.copy(isSubmitting = false)
                onSuccess(isRecycleCenter)
            } catch (e: Exception) {
                uiState = uiState.copy(isSubmitting = false)
                onError(e.message ?: "Error al enviar")
            }
        }
    }
}

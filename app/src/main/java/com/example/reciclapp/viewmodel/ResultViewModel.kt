package com.example.reciclapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.data.recycle.RecycleRepository
import com.example.reciclapp.model.WasteType
import com.example.reciclapp.screens.camera.ResultUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultViewModel(
    private val repository: RecycleRepository =
        RecycleRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState

    /** Cargar info del tacho según el tipo de residuo */
    fun loadForWasteType(wasteTypeName: String) {
        if (_uiState.value.wasteType != null) return  // ya cargado

        val wasteType = WasteType.fromDisplayName(wasteTypeName)
        if (wasteType == null) {
            _uiState.update {
                it.copy(errorMessage = "Tipo de residuo desconocido: $wasteTypeName")
            }
            return
        }

        _uiState.update {
            it.copy(
                wasteType = wasteType,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val recycleBin = repository.getRecycleBinForWaste(wasteType)
                _uiState.update {
                    it.copy(
                        recycleBin = recycleBin,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar datos de reciclaje"
                    )
                }
            }
        }
    }

    /** Llamar cuando el usuario toca "Ya Reciclé" */
    fun confirmRecycling() {
        val bin = _uiState.value.recycleBin ?: return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                repository.addPointsAndMetricsForCurrentUser(bin)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        recyclingCompleted = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No se pudieron registrar los puntos"
                    )
                }
            }
        }
    }
}

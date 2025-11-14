package com.example.reciclapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.data.recycle.RecycleRepository
import com.example.reciclapp.model.RecycleBin
import com.example.reciclapp.model.WasteType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultUiState(
    val wasteType: WasteType? = null,
    val recycleBin: RecycleBin? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recyclingCompleted: Boolean = false
)

class ResultViewModel(
    private val repository: RecycleRepository =
        RecycleRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState

    fun loadForWasteType(wasteTypeName: String) {
        if (_uiState.value.wasteType != null) return

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

    /**
     * Registrar reciclaje:
     * - Actualiza totales del usuario.
     * - Crea documento en users/{uid}/residuos.
     *
     * @param imageUrl URL de la imagen capturada (si la tienes; puede ser null por ahora).
     */
    fun confirmRecycling(imageUrl: String? = null) {
        val currentState = _uiState.value
        val bin = currentState.recycleBin ?: return
        val wasteType = currentState.wasteType ?: return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                repository.registerRecycleForCurrentUser(
                    recycleBin = bin,
                    wasteTypeDocId = wasteType.binDocId,
                    imageUrl = imageUrl
                )

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

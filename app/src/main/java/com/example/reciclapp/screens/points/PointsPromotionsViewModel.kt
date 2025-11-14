package com.example.reciclapp.screens.points

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.reciclapp.model.Promotion

data class PointsPromotionsUiState(
    val isLoading: Boolean = true,
    val totalPoints: Int = 0,
    val promotions: List<Promotion> = emptyList(),
    val errorMessage: String? = null
)

class PointsPromotionsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(PointsPromotionsUiState())
    val uiState: StateFlow<PointsPromotionsUiState> = _uiState.asStateFlow()

    init {
        loadTotalPoints()
        loadPromotions()
    }

    // Lee totalPoints del documento de usuario
    private fun loadTotalPoints() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Error al cargar puntos",
                            isLoading = false
                        )
                    }
                    return@addSnapshotListener
                }

                val totalPoints = snapshot?.getLong("totalPoints")?.toInt() ?: 0

                _uiState.update {
                    it.copy(
                        totalPoints = totalPoints,
                        isLoading = false
                    )
                }
            }
    }

    // Lee promociones de la colección "promotions" (cámbiale el nombre si quieres "promociones")
    private fun loadPromotions() {
        db.collection("promotions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Error al cargar promociones",
                            isLoading = false
                        )
                    }
                    return@addSnapshotListener
                }

                val promos = snapshot?.documents?.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()
                    Promotion(
                        id = doc.id,
                        descripcion = data["descripcion"] as? String ?: "",
                        porcentaje = (data["porcentaje"] as? Number)?.toInt() ?: 0,
                        puntos = (data["puntos"] as? Number)?.toInt() ?: 0,
                        cadena = data["cadena"] as? String ?: ""
                    )
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        promotions = promos,
                        isLoading = false
                    )
                }
            }
    }
}
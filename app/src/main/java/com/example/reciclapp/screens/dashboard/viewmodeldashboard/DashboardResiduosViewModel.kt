package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.screens.dashboard.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardResiduosViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    // Map para almacenar los datos por UID
    private val _residuosMap = mutableMapOf<String, MutableStateFlow<Map<String, Int>>>()

    // Función que retorna un StateFlow para Compose
    fun getResiduosCount(uid: String): StateFlow<Map<String, Int>> {
        // Si ya existe un StateFlow para este UID, lo devolvemos
        val existingFlow = _residuosMap[uid]
        if (existingFlow != null) return existingFlow

        // Si no existe, creamos uno nuevo
        val stateFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
        _residuosMap[uid] = stateFlow

        // Lanzamos la recolección de datos desde Firestore
        viewModelScope.launch {
            repository.getResiduosCount(uid).collect { countMap ->
                stateFlow.value = countMap
            }
        }

        return stateFlow
    }
}
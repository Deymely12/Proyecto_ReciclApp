package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.screens.dashboard.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardImpactoViewModel(
    private val repository: FirestoreRepository // tu repositorio de Firebase
) : ViewModel() {

    private val _impactoMap = MutableStateFlow<Map<String, Double>>(emptyMap())
    val impactoMap: StateFlow<Map<String, Double>> = _impactoMap

    fun loadImpacto(uid: String) {
        viewModelScope.launch {
            repository.getImpacto(uid).collect { map ->
                _impactoMap.value = map
            }
        }
    }
}

class DashboardImpactoViewModelFactory(
    private val repository: FirestoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardImpactoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardImpactoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
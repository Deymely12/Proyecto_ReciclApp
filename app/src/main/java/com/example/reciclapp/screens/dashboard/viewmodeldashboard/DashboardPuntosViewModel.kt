package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import androidx.lifecycle.ViewModel
import com.example.reciclapp.screens.dashboard.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.Flow

class DashboardPuntosViewModel(
    private val repo: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    fun getPuntosPorDia(uid: String): Flow<Map<String, Int>> = repo.getPuntosPorDia(uid)
}
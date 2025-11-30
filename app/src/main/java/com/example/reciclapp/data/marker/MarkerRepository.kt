package com.example.reciclapp.data.marker
import com.example.reciclapp.model.Marker
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    fun getActiveMarkers(): Flow<List<Marker>>

    // Para registrar un marcador desde la RegisterScreen
    suspend fun registerMarker(marker: Marker)
}
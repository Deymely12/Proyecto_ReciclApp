package com.example.reciclapp.data.marker
import com.example.reciclapp.model.Marker
import kotlinx.coroutines.flow.Flow

interface MarkerRepository {
    fun getActiveMarkers(): Flow<List<Marker>>
}
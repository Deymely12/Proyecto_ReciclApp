package com.example.reciclapp.screens.map

import com.example.reciclapp.model.Marker
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val userLocation: LatLng? = null,
    val markers: List<Marker> = emptyList(),
    val nearestMarker: Marker? = null,
    val distanceToNearest: Float? = null,
    val topNearest: List<Pair<Marker, Float>> = emptyList(),
    val isLoadingLocation: Boolean = false,
    val errorMessage: String? = null
)
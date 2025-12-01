package com.example.reciclapp.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.data.location.LocationDataSource
import com.example.reciclapp.data.marker.FirebaseMarkerRepository
import com.example.reciclapp.data.marker.MarkerRepository
import com.example.reciclapp.model.Marker
import com.example.reciclapp.screens.map.MapUiState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val markerRepository: MarkerRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        observeMarkers()
        listenUserRole()
    }

    private fun observeMarkers() {
        viewModelScope.launch {
            markerRepository.getActiveMarkers()
                .onEach { markers ->
                    _uiState.value = _uiState.value.copy(markers = markers)
                    recalculateNearest()
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error al cargar marcadores: ${e.message}"
                    )
                }
                .collect { }
        }
    }

    fun onLocationPermissionGranted() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            val location = locationDataSource.getLastKnownLocation()
            _uiState.value = _uiState.value.copy(
                userLocation = location,
                isLoadingLocation = false
            )
            recalculateNearest()
        }
    }

    private fun recalculateNearest() {
        val user = _uiState.value.userLocation
        val markers = _uiState.value.markers

        if (user == null || markers.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                nearestMarker = null,
                distanceToNearest = null,
                topNearest = emptyList()
            )
            return
        }

        val top = topNNearest(user, markers, 5)
        val first = top.firstOrNull()

        _uiState.value = _uiState.value.copy(
            nearestMarker = first?.first,
            distanceToNearest = first?.second,
            topNearest = top
        )
    }

    private fun topNNearest(
        userLatLng: LatLng,
        markers: List<Marker>,
        n: Int
    ): List<Pair<Marker, Float>> {
        val userLoc = Location("User").apply {
            latitude = userLatLng.latitude
            longitude = userLatLng.longitude
        }

        return markers
            .map { m ->
                val markerLoc = Location("Marker").apply {
                    latitude = m.latitude
                    longitude = m.longitude
                }
                m to userLoc.distanceTo(markerLoc)
            }
            .sortedBy { it.second }
            .take(n)
    }

    private fun listenUserRole() {
        val uid = auth.currentUser?.uid ?: return  // si no hay usuario logueado, se queda en false

        firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // si hay error, no mostramos el botón
                    _uiState.update { it.copy(canSeeRequests = false) }
                    return@addSnapshotListener
                }

                val rol = snapshot?.getBoolean("rol") ?: false
                _uiState.update { it.copy(canSeeRequests = rol) }
            }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val firestore = FirebaseFirestore.getInstance()
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val markerRepository = FirebaseMarkerRepository(firestore)
            val locationDataSource = LocationDataSource(fusedClient)

            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MapViewModel(markerRepository, locationDataSource) as T
                }
            }
        }
    }
}
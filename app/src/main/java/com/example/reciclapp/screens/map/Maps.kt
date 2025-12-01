package com.example.reciclapp.screens.map

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import com.example.reciclapp.viewmodel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    // Pedimos permiso una sola vez al entrar
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }

    val defaultLocation = LatLng(-12.046374, -77.042793) // Lima
    val startCameraPosition = CameraPosition.fromLatLngZoom(
        uiState.userLocation ?: defaultLocation,
        14f
    )
    val cameraPositionState = rememberCameraPositionState {
        position = startCameraPosition
    }

    // Movemos cámara cuando cambia la ubicación del usuario
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { latLng ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(latLng, 15f)
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // Ubicación del usuario
                uiState.userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Mi Ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Marcadores desde Firebase
                uiState.markers.forEach { marker ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(marker.latitude, marker.longitude)
                        ),
                        title = marker.name,
                        snippet = marker.description
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { navController.navigate("register") }) {
                Text("Registrar Ubicación")
            }

            // Solo mostramos el botón si el usuario tiene rol == true
            if (uiState.canSeeRequests) {
                Button(onClick = { navController.navigate("request") }) {
                    Text("Solicitudes")
                }
            }
        }

    }
}
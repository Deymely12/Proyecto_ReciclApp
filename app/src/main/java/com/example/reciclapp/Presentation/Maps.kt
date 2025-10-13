package com.example.reciclapp.Presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.example.reciclapp.model.Coordinate


@Composable
fun MapNavigator() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "maps") {
        composable("maps") {
            MapScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("request") {
            RequestListScreen(navController = navController)
        }
    }
}

@Composable
fun MapScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current


    // Cliente para obtener la ubicación del dispositivo
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Estados para almacenar la información
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var markersFromFirebase by remember { mutableStateOf<List<Coordinate>>(emptyList()) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para solicitar permisos de ubicación
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    // Efecto para solicitar permiso al iniciar el composable si no se tiene
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    //obtener la última ubicación conocida del usuario
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { userLocation = LatLng(it.latitude, it.longitude) }
                }
            } catch (e: SecurityException) {
                println("Security exception: ${e.message}")
            }
        }
    }

    //nos suscribimos a los cambios de la colección en Firebase
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listenerRegistration = db.collection("marker")
            .whereEqualTo("state", true) // Solo muestra marcadores habilitados
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening for updates: $error")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    markersFromFirebase = snapshot.documents.mapNotNull { document ->
                        val data = document.data
                        val name = data?.get("name") as? String
                        val coordinates = data?.get("coordinates") as? GeoPoint
                        val description = data?.get("description") as? String
                        val state = data?.get("state") as? Boolean
                        if (name != null && coordinates != null) {
                            Coordinate(name, coordinates, description, state)
                        } else {
                            null
                        }
                    }
                }
            }
        onDispose { listenerRegistration.remove()
        }
    }

    // Estado para la cámara del mapa. La centramos en la ubicación del usuario o en una por defecto.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation ?: LatLng(-12.046374, -77.042793), // Ubicación por defecto (Lima) si no hay permiso
            14f
        )
    }

    // Cuando se obtiene la ubicación del usuario, movemos la cámara
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }
    Card(modifier=modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor del mapa que ocupa la mayor parte del espacio para el mapa
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = true)
                ) {
                    // Este marcador solo aparecerá si se concedió el permiso y se obtuvo la ubicación
                    userLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Mi Ubicación",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }

                    // Estos marcadores aparecerán siempre
                    markersFromFirebase.forEach { markerData ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    markerData.coordinates.latitude,
                                    markerData.coordinates.longitude
                                )
                            ),
                            title = markerData.name,
                            snippet = markerData.description
                        )
                    }
                }
            }

            // Fila con los botones de acción
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
                Button(onClick = { navController.navigate("request") }) {
                    Text("Solicitudes")
                }
            }
        }
    }
}


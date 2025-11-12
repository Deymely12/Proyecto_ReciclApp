package com.example.reciclapp.screens.camera

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.R
import com.example.reciclapp.model.Coordinate
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

// --- Data class local para el estado de esta pantalla ---
private data class ResultUiState(
    val userLocation: LatLng? = null,
    val nearestMarker: Coordinate? = null,
    val distanceToMarker: Float? = null,
    val analysisResult: String = "Botella de plastico", // Simulado
    val binType: String = "Tacho Blanco", // Simulado
    val binImageRes: Int = 0, // Placeholder
    val analyzedImageRes: Int = 0 // Placeholder
)

// --- Colores de la App (simulados de tu imagen) ---


@Composable
fun ResultScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var uiState by remember { mutableStateOf(ResultUiState()) }

    // --- LÓGICA DE VM MOVIDA A LA VISTA ---
    // Se ejecuta una vez cuando la pantalla se carga
    @SuppressLint("MissingPermission")
    LaunchedEffect(Unit) {
        try {
            // 1. Obtener la ubicación actual del usuario
            val location = fusedLocationClient.lastLocation.await()
            val userLatLng = location?.let { LatLng(it.latitude, it.longitude) }

            if (userLatLng != null) {
                // 2. Obtener la lista de marcadores aprobados de Firestore
                val markersSnapshot = FirebaseFirestore.getInstance().collection("marker")
                    .whereEqualTo("state", true)
                    .get()
                    .await()

                val markers = markersSnapshot.documents.mapNotNull { it.toObject(Coordinate::class.java) }

                if (markers.isNotEmpty()) {
                    // 3. Encontrar el marcador más cercano
                    val (nearest, distance) = findNearestMarker(userLatLng, markers)
                    uiState = uiState.copy(
                        userLocation = userLatLng,
                        nearestMarker = nearest,
                        distanceToMarker = distance
                    )
                } else {
                    uiState = uiState.copy(userLocation = userLatLng)
                }
            }
        } catch (e: Exception) {
            // Manejar error (ej. permisos denegados, GPS apagado)
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Cabecera
        AppHeader()

        // 2. Sección de Análisis
        AnalysisSection(
            imageRes = uiState.analyzedImageRes,
            resultText = uiState.analysisResult
        )

        AppDivider()

        // 3. Sección de Almacenamiento
        StorageSection(
            imageRes = uiState.binImageRes,
            binName = uiState.binType
        )

        AppDivider()

        // 4. Sección de Mapa
        MapSection(
            userLocation = uiState.userLocation,
            nearestMarker = uiState.nearestMarker,
            distance = uiState.distanceToMarker
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Botón
        Button(
            onClick = { navController.popBackStack() }, // Vuelve al mapa principal
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ya Recicle", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- El resto de los Composables de UI (AppHeader, AnalysisSection, etc.)
// --- se quedan igual que en la versión con Koin, ya que solo leen el 'uiState'.

@Composable
fun AppHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.reciclapplogo),
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "ReciclApp",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun AnalysisSection(imageRes: Int, resultText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Residuo analizado",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "Se ha analizado la foto",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "El residuo es una $resultText",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun StorageSection(imageRes: Int, binName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "El residuo se puede almacenar en:",
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = binName,
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0F7FA)) // Fondo celeste claro
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = binName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun MapSection(userLocation: LatLng?, nearestMarker: Coordinate?, distance: Float?) {
    val defaultLocation = LatLng(-12.046374, -77.042793)
    val markerLocation = nearestMarker?.coordinates?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerLocation, 15f)
    }

    cameraPositionState.position = CameraPosition.fromLatLngZoom(markerLocation, 15f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Centros de Reciclaje Cerca de Usted",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false
                )
            ) {
                nearestMarker?.let {
                    Marker(
                        state = MarkerState(position = markerLocation),
                        title = it.name
                    )
                }
                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Tu Ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = nearestMarker?.let {
                "Estás a ${distance?.toInt()} metros del ${it.name}"
            } ?: "Buscando centros cercanos...",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AppDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        thickness = 1.dp,
        color = Color.LightGray
    )
}

// Función de ayuda copiada del ViewModel
private fun findNearestMarker(userLocation: LatLng, markers: List<Coordinate>): Pair<Coordinate?, Float?> {
    var nearestMarker: Coordinate? = null
    var minDistance: Float = Float.MAX_VALUE

    val userLoc = Location("User")
    userLoc.latitude = userLocation.latitude
    userLoc.longitude = userLocation.longitude

    for (marker in markers) {
        val markerLoc = Location("Marker")
        markerLoc.latitude = marker.coordinates.latitude
        markerLoc.longitude = marker.coordinates.longitude

        val distance = userLoc.distanceTo(markerLoc) // Distancia en metros
        if (distance < minDistance) {
            minDistance = distance
            nearestMarker = marker
        }
    }
    return Pair(nearestMarker, minDistance)
}

@Preview(showBackground = true)
@Composable
fun Pre(){
    ResultScreen(navController = rememberNavController())
}
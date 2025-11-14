package com.example.reciclapp.screens.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.reciclapp.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await

// Modelo local robusto para esta pantalla (evita ClassCastException si tu data class difiere)
private data class MarkerItem(
    val name: String,
    val coordinates: GeoPoint,
    val description: String? = null,
    val state: Boolean? = null
)

private data class ResultUiState(
    val userLocation: LatLng? = null,
    val nearestMarker: MarkerItem? = null,
    val distanceToMarker: Float? = null,
    val topNearest: List<Pair<MarkerItem, Float>> = emptyList(),
    val analysisResult: String = "Botella de plástico", // Simulado
    val binType: String = "Tacho Blanco", // Simulado
    @DrawableRes val binImageRes: Int = 0, // Placeholder
    @DrawableRes val analyzedImageRes: Int = 0 // Placeholder
)

@Composable
fun ResultScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var uiState by remember { mutableStateOf(ResultUiState()) }
    var markersFromFirebase by remember { mutableStateOf(emptyList<MarkerItem>()) }

    //Permisos de ubicación
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            val perms = buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.ACCESS_COARSE_LOCATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Si quisieras Precise Alarms/Background; no es necesario aquí.
                }
            }.toTypedArray()
            permissionLauncher.launch(perms)
        }
    }

    //Suscripción en tiempo real a Firestore (solo state == true)
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val registration = db.collection("marker")
            .whereEqualTo("state", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log, sin crashear
                    println("Firestore listen error: $error")
                    return@addSnapshotListener
                }
                runCatching {
                    if (snapshot != null) {
                        // Parse robusto sin depender de tu data class global
                        val list = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data ?: return@mapNotNull null
                            val name = data["name"] as? String ?: return@mapNotNull null
                            val gp = data["coordinates"] as? GeoPoint ?: return@mapNotNull null
                            val desc = data["description"] as? String
                            val state = data["state"] as? Boolean
                            MarkerItem(name = name, coordinates = gp, description = desc, state = state)
                        }
                        markersFromFirebase = list
                    }
                }.onFailure { e ->
                    println("Mapping error: $e")
                }
            }
        onDispose { registration.remove() }
    }

    //Obtener ubicación del usuario una vez (cuando ya hay permiso)
    @SuppressLint("MissingPermission")
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect
        runCatching {
            val location = fusedLocationClient.lastLocation.await()
            val userLatLng = location?.let { LatLng(it.latitude, it.longitude) }
            uiState = uiState.copy(userLocation = userLatLng)
        }.onFailure { e ->
            Toast.makeText(context, "Error de ubicación", Toast.LENGTH_SHORT).show()

            println("Location error: $e")
        }
    }

    // Recalcular nearest y top-5 cuando cambien ubicación o marcadores
    LaunchedEffect(uiState.userLocation, markersFromFirebase) {
        val user = uiState.userLocation
        if (user != null && markersFromFirebase.isNotEmpty()) {
            val top = topNNearest(user, markersFromFirebase, 5)
            val first = top.first()
            uiState = uiState.copy(
                nearestMarker = first.first,
                distanceToMarker = first.second,
                topNearest = top
            )
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        /*
        AnalysisSection(
            imageRes = uiState.analyzedImageRes,
            resultText = uiState.analysisResult
        )

        AppDivider()
         */

        StorageSection(
            imageRes = uiState.binImageRes,
            binName = uiState.binType
        )

        AppDivider()

        MapSection(
            userLocation = uiState.userLocation,
            nearestMarker = uiState.nearestMarker,
            distance = uiState.distanceToMarker,
            topNearest = uiState.topNearest
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("ResultScreen") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ya Reciclé", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
/*
@Composable
fun AnalysisSection(@DrawableRes imageRes: Int, resultText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageRes != 0) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Residuo analizado",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // placeholder sin color fijo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin imagen", fontSize = 12.sp)
            }
        }

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
*/
@Composable
fun StorageSection(@DrawableRes imageRes: Int, binName: String) {
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

        if (imageRes != 0) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = binName,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            // placeholder sin color fijo
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("(sin icono)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = binName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun MapSection(
    userLocation: LatLng?,
    nearestMarker: MarkerItem?,
    distance: Float?,
    topNearest: List<Pair<MarkerItem, Float>> = emptyList()
) {
    val defaultLocation = LatLng(-12.046374, -77.042793)

    val targetLatLng: LatLng = when {
        nearestMarker != null -> nearestMarker.coordinates.toLatLng()
        else -> defaultLocation
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 15f)
    }

    // Mueve/Anima la cámara cuando cambia el destino
    LaunchedEffect(targetLatLng) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(targetLatLng, 15f)
            )
        )
    }

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
                .height(220.dp)
                .clip(RoundedCornerShape(8.dp))
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
                // Dibujar top N marcadores (candidatos más cercanos)
                topNearest.forEach { (coord, _) ->
                    Marker(
                        state = MarkerState(position = coord.coordinates.toLatLng()),
                        title = coord.name
                    )
                }
                // Marcador del centro más cercano (dibujado al final para destacar por orden)
                nearestMarker?.let { m ->
                    Marker(
                        state = MarkerState(position = m.coordinates.toLatLng()),
                        title = m.name
                    )
                }
                // Ubicación del usuario
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
                val d = distance?.toInt()
                if (d != null) "Estás a $d metros del Centro ${it.name}" else "Centro más cercano: ${it.name}"
            } ?: if (userLocation == null) {
                "Activa tu GPS o permite la ubicación para buscar centros cercanos"
            } else {
                "Buscando centros cercanos..."
            },
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        if (topNearest.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            topNearest.take(5).forEachIndexed { index, (coord, dist) ->
                Text("${index + 1}. ${coord.name} — ${dist.toInt()} m", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AppDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        thickness = 1.dp
    )
}

//Utilidades

private fun GeoPoint.toLatLng(): LatLng = LatLng(this.latitude, this.longitude)

private fun topNNearest(
    userLatLng: LatLng,
    markers: List<MarkerItem>,
    n: Int
): List<Pair<MarkerItem, Float>> {
    val userLoc = Location("User").apply {
        latitude = userLatLng.latitude
        longitude = userLatLng.longitude
    }
    return markers
        .map { m ->
            val markerLoc = Location("Marker").apply {
                latitude = m.coordinates.latitude
                longitude = m.coordinates.longitude
            }
            m to userLoc.distanceTo(markerLoc) // metros
        }
        .sortedBy { it.second }
        .take(n)
}

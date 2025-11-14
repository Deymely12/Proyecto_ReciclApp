package com.example.reciclapp.screens.camera

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.reciclapp.model.Marker
import com.example.reciclapp.model.getBinDrawableForDocId
import com.example.reciclapp.screens.camera.CongratsCard
import com.example.reciclapp.viewmodel.MapViewModel
import com.example.reciclapp.viewmodel.ResultViewModel
//import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun ResultScreen(
    navController: NavHostController,
    mapViewModel: MapViewModel,
    wasteTypeName: String,
    resultViewModel: ResultViewModel = viewModel()
) {
    val context = LocalContext.current

    // Estado del mapa (ubicación y marcadores)
    val mapUiState by mapViewModel.uiState.collectAsState()

    // Estado del resultado (tacho, puntos, métricas)
    val resultUiState by resultViewModel.uiState.collectAsState()

    // Cargar datos del doc de Firestore según el tipo de residuo
    LaunchedEffect(wasteTypeName) {
        resultViewModel.loadForWasteType(wasteTypeName)
    }

    // Si el reciclaje ya se registró, mostramos solo el CongratsCard
    if (resultUiState.recyclingCompleted && resultUiState.recycleBin != null) {
        val bin = resultUiState.recycleBin
        CongratsCard(
            navController = navController,
            points = bin?.points_per_item ?: 0,
            itemCount = 1,
            itemLabel = bin?.name?.lowercase() ?: ""   // "plástico", "vidrio", etc.
        )
        return
    }

    // ---------------- MANEJO DE PERMISOS DE UBICACIÓN ----------------
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            mapViewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            mapViewModel.onLocationPermissionGranted()
        }
    }
    // -----------------------------------------------------------------

    val recycleBin = resultUiState.recycleBin

    // Imagen y textos del tacho
    @DrawableRes
    val binImageRes = recycleBin?.let { getBinDrawableForDocId(it.id) } ?: 0
    val binColorName = recycleBin?.bin_color_name ?: "Tacho"
    val binTypeText = recycleBin?.name ?: wasteTypeName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // Parte superior: información del tacho
        when {
            resultUiState.isLoading && recycleBin == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            resultUiState.errorMessage != null && recycleBin == null -> {
                Text(
                    text = "Error: ${resultUiState.errorMessage}",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red
                )
            }

            else -> {
                StorageSection(
                    imageRes = binImageRes,
                    binName = "$binTypeText → Tacho $binColorName"
                )
            }
        }

        AppDivider()

        // Sección del mapa
        MapSection(
            userLocation = mapUiState.userLocation,
            nearestMarker = mapUiState.nearestMarker,
            distance = mapUiState.distanceToNearest,
            topNearest = mapUiState.topNearest
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mensaje de error si falla el registro de puntos
        resultUiState.errorMessage?.let { err ->
            if (recycleBin != null) {
                Text(
                    text = err,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Botón "Ya Reciclé"
        Button(
            onClick = { resultViewModel.confirmRecycling(imageUrl = null) },
            enabled = !resultUiState.isLoading && recycleBin != null,
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MapSection(
    userLocation: LatLng?,
    nearestMarker: Marker?,
    distance: Float?,
    topNearest: List<Pair<Marker, Float>>
) {
    val defaultLocation = LatLng(-12.046374, -77.042793) // Lima centro

    val targetLatLng = when {
        nearestMarker != null -> LatLng(nearestMarker.latitude, nearestMarker.longitude)
        else -> defaultLocation
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLatLng, 15f)
    }

    LaunchedEffect(targetLatLng) {
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(targetLatLng, 15f)
            )
        )
    }

    // Ruta simple (línea recta) entre usuario y centro más cercano
    val routePoints = remember(userLocation, nearestMarker) {
        buildRouteToNearest(userLocation, nearestMarker)
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
                // Marcadores de los top N centros
                topNearest.forEach { (marker, _) ->
                    com.google.maps.android.compose.Marker(
                        state = MarkerState(
                            position = LatLng(marker.latitude, marker.longitude)
                        ),
                        title = marker.name
                    )
                }

                // Centro más cercano resaltado
                nearestMarker?.let { m ->
                    com.google.maps.android.compose.Marker(
                        state = MarkerState(
                            position = LatLng(m.latitude, m.longitude)
                        ),
                        title = m.name
                    )
                }

                // Ubicación del usuario
                userLocation?.let {
                    com.google.maps.android.compose.Marker(
                        state = MarkerState(position = it),
                        title = "Tu Ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // Ruta
                if (routePoints.size >= 2) {
                    Polyline(
                        points = routePoints,
                        clickable = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = nearestMarker?.let {
                val d = distance?.toInt()
                if (d != null) "Estás a $d metros del Centro ${it.name}"
                else "Centro más cercano: ${it.name}"
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
            topNearest.take(5).forEachIndexed { index, (marker, dist) ->
                Text("${index + 1}. ${marker.name} — ${dist.toInt()} m", fontSize = 14.sp)
            }
        }
    }
}

private fun buildRouteToNearest(
    userLocation: LatLng?,
    nearestMarker: Marker?
): List<LatLng> {
    if (userLocation == null || nearestMarker == null) return emptyList()

    val destLatLng = LatLng(nearestMarker.latitude, nearestMarker.longitude)

    // Aquí es solo una línea recta: [origen, destino]
    return listOf(userLocation, destLatLng)
}

@Composable
fun AppDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        thickness = 1.dp
    )
}

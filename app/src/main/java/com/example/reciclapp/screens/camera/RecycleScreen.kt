package com.example.reciclapp.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.reciclapp.viewmodel.MapViewModel
import com.example.reciclapp.model.Marker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun ResultScreen(
    navController: NavController,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // MANEJO DE PERMISOS
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
            // Si recién dieron permiso, pedimos ubicación desde el ViewModel
            viewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }
    // ------------------------------------------------------------

    // Datos "fake" de análisis (puedes luego pasarlos desde otro ViewModel u otra capa)
    val analysisResult = "Botella de plástico"
    val binType = "Tacho Blanco"
    @DrawableRes val binImageRes = 0
    @DrawableRes val analyzedImageRes = 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        StorageSection(
            imageRes = binImageRes,
            binName = binType
        )

        AppDivider()

        MapSection(
            userLocation = uiState.userLocation,
            nearestMarker = uiState.nearestMarker,
            distance = uiState.distanceToNearest,
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
            fontSize = 18.sp
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
    val defaultLocation = LatLng(-12.046374, -77.042793)

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

    // calculamos los puntos de la ruta
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
                topNearest.forEach { (marker, _) ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(marker.latitude, marker.longitude)
                        ),
                        title = marker.name
                    )
                }

                nearestMarker?.let { m ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(m.latitude, m.longitude)
                        ),
                        title = m.name
                    )
                }

                userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Tu Ubicación",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                // trazamos la ruta solo si hay al menos 2 puntos
                if (routePoints.size >= 2) {
                    Polyline(
                        points = routePoints,
                        clickable = false
                        // si quieres puedes configurar más propiedades según la versión de la lib
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
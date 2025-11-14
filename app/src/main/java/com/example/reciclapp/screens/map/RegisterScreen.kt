package com.example.reciclapp.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.example.reciclapp.model.Coordinate

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coordinates by remember { mutableStateOf<GeoPoint?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registrar Nuevo Punto", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del Lugar") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = coordinates?.let { "Lat: %.4f, Lon: %.4f".format(it.latitude, it.longitude) } ?: "Coordenadas no obtenidas",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                coordinates = GeoPoint(it.latitude, it.longitude)
                                Toast.makeText(context, "Ubicación obtenida", Toast.LENGTH_SHORT).show()
                            } ?: Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: SecurityException) {
                        // Handle error
                    }
                } else {
                    Toast.makeText(context, "Se requiere permiso de ubicación", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obtener Ubicación Actual")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }

            Button(
                onClick = {
                    if (name.isNotBlank() && description.isNotBlank() && coordinates != null) {
                        isSubmitting = true
                        // Se crea el objeto con todos los campos requeridos por el modelo
                        val newMarker = hashMapOf(
                            "name" to name,
                            "description" to description,
                            "coordinates" to coordinates,
                            "state" to false // El sistema le asigna 'false' automáticamente
                        )

                        // Se guarda en la colección "marker"
                        FirebaseFirestore.getInstance().collection("marker")
                            .add(newMarker)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Solicitud enviada para revisión", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                                isSubmitting = false
                            }
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isSubmitting
            ) {
                Text("Enviar Solicitud")
            }
        }
    }
}


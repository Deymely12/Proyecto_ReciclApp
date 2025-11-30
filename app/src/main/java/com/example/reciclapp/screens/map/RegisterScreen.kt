package com.example.reciclapp.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.reciclapp.viewmodel.RegisterViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val uiState = viewModel.uiState

    //Estado para selección de imagen
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher: Galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageSelected(it)
        }
    }

    // Launcher: Cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.onImageSelected(photoUri)
        }
    }

    // Permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir cámara
            photoUri = createImageUri(context)
            photoUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_LONG).show()
        }
    }

    // Diálogo de selección de fuente de imagen
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Cómo deseas obtener la imagen?") },
            confirmButton = {
                // Cámara
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                photoUri = createImageUri(context)
                                photoUri?.let { uri ->
                                    takePictureLauncher.launch(uri)
                                }
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara")
                    }
                }
            },
            dismissButton = {
                // Galería
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Imagen seleccionada (preview desde Uri)
        uiState.selectedImageUri?.let { uri ->
            Card(
                modifier = Modifier
                    .size(300.dp)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para seleccionar/capturar imagen (abre el diálogo)
        Button(
            onClick = { showImageSourceDialog = true },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seleccionar Imagen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Nombre del Lugar") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.coordinates?.let {
                "Lat: %.4f, Lon: %.4f".format(it.latitude, it.longitude)
            } ?: "Coordenadas no obtenidas",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                viewModel.onCoordinatesChange(
                                    GeoPoint(it.latitude, it.longitude)
                                )
                                Toast.makeText(
                                    context,
                                    "Ubicación obtenida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } ?: Toast.makeText(
                                context,
                                "No se pudo obtener la ubicación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (_: SecurityException) {
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Se requiere permiso de ubicación",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obtener Ubicación Actual")
        }

        Spacer(modifier = Modifier.height(16.dp))

        //Botón analizar imagen con Roboflow
        if (uiState.selectedImageUri != null) {
            Button(
                onClick = { viewModel.analyzeImage(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isAnalyzingImage
            ) {
                if (uiState.isAnalyzingImage) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analizando...")
                } else {
                    Text("Analizar imagen (Roboflow)")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.isRecycleCenter) {
                true -> Text(
                    text = "Se ha detectado un centro de reciclaje en la imagen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                false -> Text(
                    text = "No se detectó un centro de reciclaje en la imagen.",
                    style = MaterialTheme.typography.bodyMedium
                )
                null -> {
                    // Aún no se analiza nada: no mostramos nada
                }
            }

            uiState.imageAnalysisError?.let { err ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error en análisis: $err",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        // Botones finales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Volver")
            }

            Button(
                onClick = {
                    viewModel.submit(
                        onSuccess = { isRecycleCenter ->
                            val msg = if (isRecycleCenter) {
                                "El modelo detectó un centro de reciclaje y se aprobo automaticamente la solicitud"
                            } else {
                                "El modelo no reconoció un centro de reciclaje, se envió la solicitud al administrador"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !uiState.isSubmitting
            ) {
                Text("Enviar Solicitud")
            }
        }
    }
}
private fun createImageUri(context: Context): Uri {
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "JPEG_${System.currentTimeMillis()}.jpg"
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

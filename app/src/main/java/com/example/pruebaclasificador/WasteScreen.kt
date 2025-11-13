package com.example.pruebaclasificador

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pruebaclasificador.WasteViewModel
import com.example.pruebaclasificador.data.Prediction
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.example.pruebaclasificador.data.ClassMapping


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteScreen(
    viewModel: WasteViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var predictions by remember { mutableStateOf<List<Prediction>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // URI temporal para la foto de la cámara
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            selectedImage = bitmap
            predictions = null
            errorMessage = null
        }
    }

    // Launcher para cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                selectedImage = bitmap
                predictions = null
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error al cargar la imagen: ${e.message}"
                Log.e("WasteScreen", "Error loading image", e)
            }
        }
    }

    // Launcher para permisos de cámara
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
            errorMessage = "Permiso de cámara denegado"
        }
    }

    // Diálogo para seleccionar origen de imagen
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Cómo deseas obtener la imagen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        // Verificar y solicitar permiso de cámara
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                // Permiso ya concedido
                                photoUri = createImageUri(context)
                                photoUri?.let { uri ->
                                    takePictureLauncher.launch(uri)
                                }
                            }
                            else -> {
                                // Solicitar permiso
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clasificador de Residuos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Mostrar la imagen seleccionada
            selectedImage?.let {
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para seleccionar imagen (muestra diálogo)
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

            // Botón para clasificar imagen
            Button(
                onClick = {
                    selectedImage?.let { bitmap ->
                        isLoading = true
                        errorMessage = null
                        viewModel.classifyImage(bitmap) { result ->
                            isLoading = false
                            predictions = result
                            if (result == null) {
                                errorMessage = "Error al clasificar la imagen"
                            }
                        }
                    }
                },
                enabled = selectedImage != null && !isLoading,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clasificando...")
                } else {
                    Text("Clasificar Imagen")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar resultados
            predictions?.let { predList ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultados:",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (predList.isEmpty()) {
                            Text(
                                text = "No se detectaron objetos en la imagen",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            predList.forEachIndexed { index, prediction ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    val mappedCategory = ClassMapping.mapping[prediction.`class`] ?: "Otros residuos"

                                    Text(
                                        //text = "🗑️ ${prediction.`class`}",
                                        text = "🗑️ ${mappedCategory} (${prediction.`class`})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Confianza: ${(prediction.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (index < predList.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Mostrar error si hay
            errorMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// Función helper para crear URI de imagen
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
package com.example.reciclapp.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.reciclapp.R
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    //observa el estado actual de autenticación
    val authState by authViewModel.authState.collectAsState()

    //verificar si el estado de autenticación es de tipo Success
    val user = (authState as? com.example.reciclapp.viewmodel.AuthState.Success)?.user

    //almacenan el nombre y apellido del usuario
    var firstname by remember { mutableStateOf(user?.firstname ?: "") }
    var lastname by remember { mutableStateOf(user?.lastname ?: "") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    //prepara un launcher para selector de archivo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Foto de perfil circular con picker
        val painter = photoUri?.let { rememberAsyncImagePainter(it) }
            ?: if (!user?.photoUrl.isNullOrEmpty()) rememberAsyncImagePainter(user!!.photoUrl)
            else painterResource(id = R.drawable.ic_perfil)

        Image(
            painter = painter,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre y Apellido
        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Cancelar")
            }
            Button(onClick = {
                authViewModel.updateUserProfile(firstname, lastname, photoUri)
                onBack()
            }) {
                Text("Guardar")
            }
        }
    }
}

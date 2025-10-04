package com.example.reciclapp.screens.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.reciclapp.R
import com.example.reciclapp.viewmodel.AuthState
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    val user = (authState as? AuthState.Success)?.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Foto de perfil circular
        val painter = if (!user?.photoUrl.isNullOrEmpty()) {
            rememberAsyncImagePainter(user!!.photoUrl)
        } else {
            painterResource(id = R.drawable.ic_perfil)
        }

        Image(
            painter = painter,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre y correo
        Text(
            text = "${user?.firstname ?: ""} ${user?.lastname ?: ""}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = user?.email ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Editar perfil
        Button(
            onClick = onEditProfileClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Cambiar contraseña
        Button(
            onClick = onChangePasswordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar contraseña")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Cerrar sesión
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión", color = Color.White)
        }
    }
}

package com.example.reciclapp.screens.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun CameraScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Pantalla de Cámara")
        Button(onClick = {
            navController.navigate("analysis")
        }) {
            Text("escanear")
        }
    }
}


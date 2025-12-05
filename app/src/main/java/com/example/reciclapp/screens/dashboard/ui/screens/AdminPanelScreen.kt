package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun AdminPanelScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        MenuCard(
            title = "Dashboard Administrador",
            onClick = { navController.navigate("adminDashboard") }         )

        Spacer(modifier = Modifier.height(20.dp))

        MenuCard(
            title = "Gestión de Usuarios",
            onClick = { navController.navigate("gestionUsuarios") }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.popBackStack() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Atrás")
        }
    }
}
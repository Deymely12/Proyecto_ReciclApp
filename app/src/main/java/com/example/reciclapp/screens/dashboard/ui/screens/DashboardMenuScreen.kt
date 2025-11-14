package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun DashboardMenuScreen(navController: NavHostController, uid: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        MenuCard(
            title = "Residuos por categoría",
            onClick = { navController.navigate("residuos") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        MenuCard(
            title = "Progreso de puntos",
            onClick = { navController.navigate("puntos") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        MenuCard(
            title = "Impacto ecológico",
            onClick = { navController.navigate("impacto") }
        )
    }
}

@Composable
fun MenuCard(title: String, onClick: () -> Unit) {
    // Tarjeta limpia y bonita, sin colores personalizados
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.DashboardMenuViewModel

@Composable
fun DashboardMenuScreen(navController: NavHostController, viewModel: DashboardMenuViewModel) {
    val vm: DashboardMenuViewModel = viewModel()
    val usuario by vm.usuario.collectAsState()

    // Carga el usuario al entrar a la pantalla
    LaunchedEffect(Unit) {
        vm.cargarUsuario()
    }

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

        // --- NUEVO: Botón solo visible para Admin ---
        if (usuario?.rol == true) {
            Spacer(modifier = Modifier.height(36.dp))

            // Card llamativa usando tu tema/paleta
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(80.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { navController.navigate("adminPanel") }
                        .padding(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock, // ícono de seguridad
                            contentDescription = "Admin",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Panel de Administración",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
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
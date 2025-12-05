package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.screens.dashboard.ui.components.MonthlyGrowthChart
import com.example.reciclapp.screens.dashboard.ui.components.ResiduosBarChart
import com.example.reciclapp.screens.dashboard.ui.components.UserRoleCard
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val userRoles by viewModel.userRoles.collectAsState(initial = 0 to 0)
    val monthlyGrowth by viewModel.monthlyGrowth.collectAsState(initial = emptyList())
    val residuosData by viewModel.residuosData.collectAsState(initial = emptyList())
    val residuosFilter by viewModel.residuosFilter.collectAsState(initial = "Mes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- BOTÓN DE RETROCEDER ---
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Volver", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        // --- USUARIOS ---
        SectionBox(title = "Usuarios") {
            UserRoleCard(
                adminCount = userRoles.first,
                userCount = userRoles.second
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CRECIMIENTO MENSUAL ---
        SectionBox(title = "Crecimiento Mensual") {
            MonthlyGrowthChart(monthlyGrowth)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RESIDUOS ---
        SectionBox(title = "Residuos") {

            // Filtros: Día, Semana, Mes, Año
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Día", "Semana", "Mes", "Año").forEach { filtro ->
                    Button(
                        onClick = { viewModel.setResiduosFilter(filtro) },
                        colors = if (residuosFilter == filtro)
                            ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        else
                            ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text(text = filtro)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfico de residuos
            ResiduosBarChart(
                residuosData = residuosData,
                selectedFilter = residuosFilter,
                onFilterChange = { viewModel.setResiduosFilter(it) }
            )
        }
    }
}

@Composable
fun SectionBox(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .wrapContentHeight()
    ) {
        // Título grande y estilizado
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Contenido (gráfico o tarjeta)
        content()

        Spacer(modifier = Modifier.height(8.dp))

        // Separador bonito
        Divider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            thickness = 2.dp,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.screens.dashboard.ui.components.LineChart
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.DashboardPuntosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardPuntosScreen(uid: String, navController: NavController) {
    val vm: DashboardPuntosViewModel = viewModel()
    val puntosPorDia by vm.getPuntosPorDia(uid).collectAsState(initial = emptyMap())

    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("d/M", Locale.getDefault())

    // Ordenamos por fecha y acumulamos
    val sortedEntries = puntosPorDia.entries
        .map { it.key to it.value }
        .sortedBy { inputFormat.parse(it.first) }

    val acumulativoMap = linkedMapOf<String, Int>()
    var total = 0
    for ((fecha, puntos) in sortedEntries) {
        total += puntos
        val label = outputFormat.format(inputFormat.parse(fecha)!!)
        acumulativoMap[label] = total
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progreso de Puntos") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cuadro del gráfico
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    LineChart(
                        data = acumulativoMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de retroceder fuera del cuadro
                Button(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atrás")
                }
            }
        }
    )
}
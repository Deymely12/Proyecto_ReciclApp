package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.screens.dashboard.ui.components.DonutChart
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.DashboardResiduosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardResiduosScreen(
    uid: String,
    navController: NavController
) {
    val vm: DashboardResiduosViewModel = viewModel()
    val residuosCount by vm.getResiduosCount(uid).collectAsState(initial = emptyMap())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Residuos por Categoría") },
                navigationIcon = {}
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut y leyenda
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Card con el Donut
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(data = residuosCount, modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Leyenda manual
                    val chartColors = listOf(
                        Color.Red, Color.Green, Color.Blue, Color.Yellow,
                        Color.Cyan, Color.Magenta
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val total = residuosCount.values.sum().takeIf { it != 0 } ?: 1
                        residuosCount.entries.forEachIndexed { index, entry ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(chartColors[index % chartColors.size])
                                )
                                Text(
                                    text = "${entry.key}: ${entry.value} (${String.format("%.1f%%", entry.value * 100f / total)})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Botón de retroceder al final
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Atrás")
                    }
                }
            }
        }
    )
}
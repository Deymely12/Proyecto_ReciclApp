package com.example.reciclapp.screens.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciclapp.screens.dashboard.data.repository.FirestoreRepository
import com.example.reciclapp.screens.dashboard.ui.components.HorizontalBarChart
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.DashboardImpactoViewModel
import com.example.reciclapp.screens.dashboard.viewmodeldashboard.DashboardImpactoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardImpactoScreen(uid: String, navController: NavController) {
    val repository = FirestoreRepository() // instancia real
    val vm: DashboardImpactoViewModel = viewModel(
        factory = DashboardImpactoViewModelFactory(repository)
    )

    LaunchedEffect(uid) {
        vm.loadImpacto(uid)
    }

    val impacto by vm.impactoMap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Impacto Ecológico",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            HorizontalBarChart(
                data = impacto,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Atrás")
        }
    }
}

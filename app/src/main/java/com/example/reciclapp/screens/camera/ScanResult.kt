package com.example.reciclapp.screens.camera

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun ScanResultScreen(
    navController: NavController
) {
    //val wasteTypeName = "Orgánico"
    //val wasteTypeName = "Vidrio"
    //val wasteTypeName = "Plásticos y envases metálicos"
    val wasteTypeName = "Papel y cartón"
    //val wasteTypeName = "Otros residuos"
    Button(onClick = {
        navController.navigate("result/$wasteTypeName")
    }) {
        Text("Ver resultado")
    }
}

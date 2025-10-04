package com.example.reciclapp.screens.points

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun PointsScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Pantalla de Points")
    }
}


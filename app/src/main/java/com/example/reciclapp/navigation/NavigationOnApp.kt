package com.example.reciclapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.reciclapp.screens.home.Footer
import com.example.reciclapp.screens.home.Header

@Composable
fun MainLayout(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Header(navController)

        // Body dinámico
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        // Footer
        Footer(navController)
    }
}





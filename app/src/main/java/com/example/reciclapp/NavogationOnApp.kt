package com.example.reciclapp


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.reciclapp.Presentation.Header
import com.example.reciclapp.Presentation.MapScreen
import com.example.reciclapp.Presentation.Footer
import com.example.reciclapp.Presentation.MapNavigator
import com.example.reciclapp.Presentation.news

@Composable
fun MainLayout(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        //cabecera
        Header(navController)

        Box(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("dashboard") { /* HomeScreen(navController) */ }
                composable("map") { MapNavigator() }
                composable("points") { /* LikesScreen(navController) */ }
                composable("camera") { /* StatsScreen(navController) */ }
                composable("profile") { /* ProfileScreen(navController) */ }
                composable("home") { news() }
            }
        }

        //box
        Footer(navController)
    }
}




package com.example.reciclapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.screens.camera.ResultScreen
import com.example.reciclapp.screens.map.MapScreen
import com.example.reciclapp.screens.map.RegisterScreen
import com.example.reciclapp.screens.map.RequestListScreen
import com.example.reciclapp.screens.puntoReciclaje.MarkerListScreen
import com.example.reciclapp.viewmodel.AuthViewModel
import com.example.reciclapp.viewmodel.MapViewModel
import com.example.reciclapp.viewmodel.MapViewModel.Companion.provideFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MapNavigator(authViewModel: AuthViewModel = viewModel()) {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current


    // ViewModel compartido entre MapScreen y ResultScreen
    val mapViewModel: MapViewModel = viewModel(
        factory = provideFactory(context)
    )

    NavHost(navController = navController, startDestination = "maps") {
        composable("maps") {
            MapScreen(
                navController = navController,
                viewModel = mapViewModel
            )
        }
        composable("register") {
            RegisterScreen(navController = navController) // igual que antes
        }
        composable("request") {
            RequestListScreen(navController = navController) // igual que antes
        }
        // puntosReciclaje
        composable("puntosreciclaje") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MarkerListScreen(navController = navController,uid)
        }
    }
}
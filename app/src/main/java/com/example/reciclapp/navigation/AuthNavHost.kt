package com.example.reciclapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.screens.auth.LoginScreenAuth
import com.example.reciclapp.screens.auth.RegisterScreenAuth
import com.example.reciclapp.screens.dashboard.DashboardScreen
import com.example.reciclapp.screens.map.MapScreen
import com.example.reciclapp.screens.points.PointsScreen
import com.example.reciclapp.screens.camera.CameraScreen
import com.example.reciclapp.screens.noticias.NoticiasScreen
import com.example.reciclapp.screens.profile.ChangePasswordScreen
import com.example.reciclapp.screens.profile.EditProfileScreen
import com.example.reciclapp.screens.profile.ProfileScreen
import com.example.reciclapp.viewmodel.AuthViewModel
import com.example.reciclapp.viewmodel.NoticiasViewModel

@Composable
fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val noticiasViewModel: NoticiasViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        // --- AUTH ---
        composable("login") {
            LoginScreenAuth(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreenAuth(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate("login") },
                onGoogleRegisterClick = { /* Implementa Google register si deseas */ }
            )
        }

        // --- MAIN SCREENS con Header y Footer ---
        composable("dashboard") {
            MainLayout(navController) {
                DashboardScreen(navController)
            }
        }

        composable("map") {
            MainLayout(navController) {
                MapScreen(navController)
            }
        }

        composable("points") {
            MainLayout(navController) {
                PointsScreen(navController)
            }
        }

        composable("camera") {
            MainLayout(navController) {
                CameraScreen(navController)
            }
        }

        composable("noticias") {
            MainLayout(navController) {
                NoticiasScreen(noticiasViewModel,authViewModel)
            }
        }

        // --- PROFILE ---
        composable("profile") {
            MainLayout(navController) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onEditProfileClick = { navController.navigate("editProfile") },
                    onChangePasswordClick = { navController.navigate("changePassword") },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
        }

        // --- EDIT PROFILE ---
        composable("editProfile") {
            MainLayout(navController) {
                EditProfileScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // --- CHANGE PASSWORD ---
        composable("changePassword") {
            MainLayout(navController) {
                ChangePasswordScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}


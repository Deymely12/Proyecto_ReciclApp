package com.example.reciclapp.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reciclapp.map.MapNavigator
import com.example.reciclapp.screens.auth.LoginScreenAuth
import com.example.reciclapp.screens.auth.RegisterScreenAuth
import com.example.reciclapp.screens.dashboard.DashboardScreen
import com.example.reciclapp.screens.points.PointsScreen
import com.example.reciclapp.screens.camera.CameraScreen
<<<<<<< Updated upstream
=======
import com.example.reciclapp.screens.noticias.NoticiasFavoritasScreen
//import com.example.reciclapp.screens.noticias.NoticiasFavoritasScreen
>>>>>>> Stashed changes
import com.example.reciclapp.screens.noticias.NoticiasScreen
import com.example.reciclapp.screens.profile.ChangePasswordScreen
import com.example.reciclapp.screens.profile.EditProfileScreen
import com.example.reciclapp.screens.profile.ProfileScreen
import com.example.reciclapp.viewmodel.AuthViewModel
import com.example.reciclapp.viewmodel.NoticiasViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun AuthNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val noticiasViewModel: NoticiasViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as Activity

    // Launcher para Google Sign-In (usaremos para login y registro)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            // Aquí necesitamos saber si es login o registro
            // Para simplificar, se guardará temporalmente un flag en el ViewModel
            val isRegister = authViewModel.isRegisterFlow
            authViewModel.handleGoogleSignInResult(task, isRegister)
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        // LOGIN
        composable("login") {
            LoginScreenAuth(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") },
                onGoogleLoginClick = {
                    // Indicamos que es login
                    authViewModel.initGoogleSignIn(activity, isRegister = false)
                    launcher.launch(authViewModel.getGoogleSignInIntent()!!)
                }
            )
        }

        // REGISTRO
        composable("register") {
            RegisterScreenAuth(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate("login") },
                onGoogleRegisterClick = {
                    // Indicamos que es registro
                    authViewModel.initGoogleSignIn(activity, isRegister = true)
                    launcher.launch(authViewModel.getGoogleSignInIntent()!!)
                }
            )
        }

        // DASHBOARD
        composable("dashboard") {
            MainLayout(navController) { DashboardScreen(navController) }
        }

        // MAPA
        composable("map") {
            MainLayout(navController) { MapNavigator() }
        }

        // PUNTOS
        composable("points") {
            MainLayout(navController) { PointsScreen(navController) }
        }

        // CÁMARA
        composable("camera") {
            MainLayout(navController) { CameraScreen(navController) }
        }

        // NOTICIAS
        composable("noticias") {
            MainLayout(navController) { NoticiasScreen(noticiasViewModel, authViewModel) }
        }

        // PERFIL
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

        composable("editProfile") {
            MainLayout(navController) {
                EditProfileScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() })
            }
        }

        composable("changePassword") {
            MainLayout(navController) {
                ChangePasswordScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

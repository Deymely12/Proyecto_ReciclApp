package com.example.reciclapp.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pruebaclasificador.WasteScreen
import com.example.reciclapp.screens.auth.LoginScreenAuth
import com.example.reciclapp.screens.auth.RegisterScreenAuth
import com.example.reciclapp.screens.points.PointsPromotionsScreen
import com.example.reciclapp.screens.camera.ResultScreen
import com.example.reciclapp.screens.dashboard.ui.screens.DashboardImpactoScreen
import com.example.reciclapp.screens.dashboard.ui.screens.DashboardMenuScreen
import com.example.reciclapp.screens.dashboard.ui.screens.DashboardPuntosScreen
import com.example.reciclapp.screens.dashboard.ui.screens.DashboardResiduosScreen
import com.example.reciclapp.viewmodel.MapViewModel
import com.example.reciclapp.screens.noticias.NoticiasFavoritasScreen
import com.example.reciclapp.screens.noticias.NoticiasScreen
import com.example.reciclapp.screens.noticias.NoticiasViewModel
import com.example.reciclapp.screens.points.PromotionDetailScreen
import com.example.reciclapp.screens.profile.ChangePasswordScreen
import com.example.reciclapp.screens.profile.EditProfileScreen
import com.example.reciclapp.screens.profile.ProfileScreen
import com.example.reciclapp.screens.puntoReciclaje.MarkerListScreen
import com.example.reciclapp.screens.ranking.RankingScreen
import com.example.reciclapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthNavHost( //es el “centro de rutas”
    navController: NavHostController = rememberNavController(), //Poder movernos entre pantallas
    authViewModel: AuthViewModel = viewModel() //ViewModel principal para autenticación
) {
    val noticiasViewModel: NoticiasViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as Activity
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModel.provideFactory(context)
    )

    // Launcher para Google Sign-In (usados para login y registro)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            // Verifica si el flujo actual es de registro o de login
            val isRegister = authViewModel.isRegisterFlow
            // Maneja el resultado del inicio de sesión con Google
            authViewModel.handleGoogleSignInResult(task, isRegister)
        }
    }

    // Contenedor principal de navegación
    NavHost(navController = navController, startDestination = "login") {

        // LOGIN
        composable("login") {
            LoginScreenAuth(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("noticias") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") },// Navega a la pantalla de registro
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
                    navController.navigate("noticias") {
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

        // Menu
        composable("dashboardMenu") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainLayout(navController) {
                DashboardMenuScreen(navController, uid)
            }
        }

// Residuos
        composable("residuos") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainLayout(navController) {
                DashboardResiduosScreen(uid, navController)
            }
        }

// Puntos
        composable("puntos") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainLayout(navController) {
                DashboardPuntosScreen(uid, navController)
            }
        }

// Impacto
        composable("impacto") {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            MainLayout(navController) {
                DashboardImpactoScreen(uid, navController)
            }
        }

        // MAPA
        composable("map") {
            MainLayout(navController) { MapNavigator() }
        }

        // PUNTOS
        composable("points") {
            MainLayout(navController) {
                PointsPromotionsScreen(navController)
            }
        }

        //Detalles de Promo
        composable(
            route = "promotion_detail/{promotionId}",
            arguments = listOf(
                navArgument("promotionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val promoId = backStackEntry.arguments?.getString("promotionId") ?: ""
            PromotionDetailScreen(
                navController = navController,
                promotionId = promoId
            )
        }

        // CÁMARA
        composable("camera") {
            MainLayout(navController) {
                WasteScreen(navController)
            }
        }

        // Ranking
        composable("ranking") {
            MainLayout(navController) {
                RankingScreen(navController)
            }
        }



        // Resultados de Scaneo pt.1
        /*composable("analysis") {
            MainLayout(navController) {
                ScanResultScreen(navController)
            }
        }*/

        // Resultados de Scaneo pt.2
        composable(
            route = "result/{wasteTypeName}",
            arguments = listOf(navArgument("wasteTypeName") { type = NavType.StringType })
        ) { backStackEntry ->

            val wasteTypeName = backStackEntry.arguments?.getString("wasteTypeName") ?: "Desconocido"

            MainLayout(navController) {
                ResultScreen(
                    navController = navController,
                    mapViewModel = mapViewModel,
                    wasteTypeName = wasteTypeName
                )
            }
        }

        // NOTICIAS
        composable("noticias") {
            MainLayout(navController) { NoticiasScreen(noticiasViewModel, authViewModel) }
        }
        //NOTICIAS FAVORITAS
        composable("noticiasFavoritas") {
            MainLayout(navController) { NoticiasFavoritasScreen(authViewModel) }
        }

        // PERFIL
        composable("profile") {
            MainLayout(navController) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onEditProfileClick = { navController.navigate("editProfile") },// Navega a editar perfil
                    onChangePasswordClick = { navController.navigate("changePassword") },// Navega a cambiar contraseña
                    onLogoutClick = {
                        // Cierra sesión y vuelve a la pantalla de login, eliminando el historial de navegacion
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
                EditProfileScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() })// Regresa a la pantalla anterior
            }
        }

        composable("changePassword") {
            MainLayout(navController) {
                ChangePasswordScreen(authViewModel = authViewModel, onBack = { navController.popBackStack() })// Regresa a la pantalla anterior
            }
        }
    }
}

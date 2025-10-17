package com.example.reciclapp.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reciclapp.R
import com.example.reciclapp.presentation.components.CustomTextField
import com.example.reciclapp.presentation.components.PasswordTextField
import com.example.reciclapp.viewmodel.AuthState
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun LoginScreenAuth(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleLoginClick: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    // ======================
    // INTERFAZ DE LOGIN
    // ======================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.reciclapplogo),
            contentDescription = "Logo ReciclApp",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            label = "Correo",
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botón Google: simplemente ejecuta la acción pasada desde AuthNavHost
        Button(
            onClick = { onGoogleLoginClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Entrar con Google")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿No tienes una cuenta? Regístrate",
            modifier = Modifier.clickable { onRegisterClick() },
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (authState) {
            is AuthState.Loading -> Text("Cargando...")
            is AuthState.Error -> Text((authState as AuthState.Error).message, color = Color.Red)
            else -> {}
        }
    }

    // Detecta si el login fue exitoso
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }
}

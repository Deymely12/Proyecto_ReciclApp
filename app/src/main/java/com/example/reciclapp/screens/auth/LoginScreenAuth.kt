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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reciclapp.R
import com.example.reciclapp.presentation.components.CustomTextField
import com.example.reciclapp.presentation.components.PasswordTextField
import com.example.reciclapp.viewmodel.AuthState
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun LoginScreenAuth(
    authViewModel: AuthViewModel = viewModel(), // ViewModel encargado de la autenticación (Firebase)
    onLoginSuccess: () -> Unit,                 // Acción a ejecutar si el login es exitoso
    onRegisterClick: () -> Unit,                // Acción al presionar "Registrarse"
    onGoogleLoginClick: () -> Unit              // Acción al presionar "Entrar con Google"
) {
    val context = LocalContext.current          // Obtener el contexto actual de la aplicación


    // Variables de estado que almacenan los datos ingresados por el usuario
    var email by remember { mutableStateOf("") }                // Guarda el texto del correo
    var password by remember { mutableStateOf("") }             // Guarda la contraseña
    var passwordVisible by remember { mutableStateOf(false) }   // Controla si la contraseña se muestra o se oculta

    // Observa el estado actual de autenticación desde el ViewModel
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
        //Logo de la aplicación
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
            onValueChange = { password = it }, // Actualiza la variable password
            label = "Contraseña",
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }, // Cambia visibilidad
            visibleIconRes = R.drawable.ic_visibility,                   // Ícono de mostrar contraseña
            hiddenIconRes = R.drawable.ic_visibility_off,                // Ícono de ocultar contraseña
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para iniciar sesión con correo y contraseña
        Button(
            onClick = { authViewModel.login(email, password) }, // Llama a la función login del ViewModel
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Google: simplemente ejecuta la acción pasada desde AuthNavHost
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

        // Texto que redirige a la pantalla de registro
        Text(
            text = stringResource(R.string.preguntaRegistro),
            modifier = Modifier.clickable { onRegisterClick() }, // Navega al registro
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Muestra el estado actual de autenticación
        when (authState) {
            is AuthState.Loading -> Text("Cargando...") // Mientras se procesa el login
            is AuthState.Error -> Text((authState as AuthState.Error).message, color = Color.Red)
            else -> {}
        }
    }

    // Detecta si el login fue exitoso
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess() // Si el estado es éxito, redirige al dashboard
    }
}

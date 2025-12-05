package com.example.reciclapp.screens.auth

import android.app.Activity
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
fun RegisterScreenAuth(
    authViewModel: AuthViewModel = viewModel(),  // ViewModel que maneja la lógica de autenticación (Firebase)
    onRegisterSuccess: () -> Unit,               // Acción que se ejecuta si el registro fue exitoso
    onLoginClick: () -> Unit,                    // Acción para ir a la pantalla de inicio de sesión
    onGoogleRegisterClick: () -> Unit            // Acción para registrarse con Google
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()  // Observa el estado actual de autenticación

    // VARIABLES PARA LOS CAMPOS DE ENTRADA
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }  //Controla si la contraseña se muestra o no
    var passwordMismatch by remember { mutableStateOf(false) } // Indica si las contraseñas no coinciden

    //Diseño de la interfaz
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
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        //Campo para el nombre
        CustomTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = "Nombre",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Campo para el apellido
        CustomTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = "Apellido",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Campo para el correo
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Campo para la contraseña
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

        Spacer(modifier = Modifier.height(8.dp))

        //Campo para repetir contraseña
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Repetir contraseña",
            passwordVisible = passwordVisible, // Muestra u oculta la contraseña
            onToggleVisibility = { passwordVisible = !passwordVisible }, // Cambia visibilidad
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Si las contraseñas no coinciden, muestra mensaje de error
        if (passwordMismatch) {
            Text("Las contraseñas no coinciden", color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        //Boton de registri
        Button(
            onClick = {
                // Verifica si las contraseñas coinciden antes de registrar
                if (password != confirmPassword) {
                    passwordMismatch = true //Muestra error
                } else {
                    passwordMismatch = false // Limpia el error
                    // Llama al ViewModel para registrar con Firebase
                    authViewModel.register(firstname, lastname, email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Registrarse", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Texto que permite volver a la pantalla de inicio de sesión
        Text(
            text = stringResource(R.string.preguntaIniciar),
            modifier = Modifier.clickable { onLoginClick() },  // Llama a la acción de ir al login
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Registro con google: ahora llama al callback que manejará el flujo en el NavHost
        Button(
            onClick = onGoogleRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrarse con Google", color = Color.Black)
            }
        }
    }

    // DETECTA SI EL REGISTRO FUE EXITOSO
    LaunchedEffect(authState) {
        // Si el estado del ViewModel indica éxito, llama a la función que navega al dashboard
        if (authState is AuthState.Success) onRegisterSuccess()
    }
}
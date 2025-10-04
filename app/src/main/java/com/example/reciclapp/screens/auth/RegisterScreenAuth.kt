package com.example.reciclapp.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reciclapp.R
import com.example.reciclapp.presentation.components.CustomTextField
import com.example.reciclapp.presentation.components.PasswordTextField
import com.example.reciclapp.viewmodel.AuthState
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun RegisterScreenAuth(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleRegisterClick: (() -> Unit)? = null,
    authViewModel: AuthViewModel = viewModel()
) {
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordMismatch by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

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

        CustomTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = "Nombre",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = "Apellido",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Correo",
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

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Repetir contraseña",
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (passwordMismatch) {
            Text(
                "Las contraseñas no coinciden",
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (password != confirmPassword) {
                    passwordMismatch = true
                } else {
                    passwordMismatch = false
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

        Text(
            text = "¿Ya tienes una cuenta? Iniciar sesión",
            modifier = Modifier.clickable { onLoginClick() },
            color = Color.Blue
        )

        Spacer(modifier = Modifier.height(24.dp))

        onGoogleRegisterClick?.let { googleClick ->
            Button(
                onClick = { googleClick() },
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
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onRegisterSuccess()
    }
}

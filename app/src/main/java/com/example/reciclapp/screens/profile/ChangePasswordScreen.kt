package com.example.reciclapp.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.reciclapp.R
import com.example.reciclapp.presentation.components.PasswordTextField
import com.example.reciclapp.viewmodel.AuthViewModel

@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibleCurrent by remember { mutableStateOf(false) }
    var passwordVisibleNew by remember { mutableStateOf(false) }
    var passwordVisibleConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        PasswordTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = "Contraseña actual",
            passwordVisible = passwordVisibleCurrent,
            onToggleVisibility = { passwordVisibleCurrent = !passwordVisibleCurrent },
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "Nueva contraseña",
            passwordVisible = passwordVisibleNew,
            onToggleVisibility = { passwordVisibleNew = !passwordVisibleNew },
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off
        )

        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Repetir nueva contraseña",
            passwordVisible = passwordVisibleConfirm,
            onToggleVisibility = { passwordVisibleConfirm = !passwordVisibleConfirm },
            visibleIconRes = R.drawable.ic_visibility,
            hiddenIconRes = R.drawable.ic_visibility_off
        )

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Cancelar")
            }
            Button(onClick = {
                if (newPassword != confirmPassword) {
                    errorMessage = "Las contraseñas no coinciden"
                } else {
                    authViewModel.changePassword(currentPassword, newPassword)
                    onBack()
                }
            }) {
                Text("Cambiar")
            }
        }
    }
}


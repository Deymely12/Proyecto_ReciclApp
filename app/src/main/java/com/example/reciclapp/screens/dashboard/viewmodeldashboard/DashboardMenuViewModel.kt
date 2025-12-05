package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.screens.dashboard.data.local.UsuarioEntity
import com.example.reciclapp.screens.dashboard.data.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardMenuViewModel(
    private val repo: UsuarioRepository // ahora usamos UsuarioRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioEntity?>(null)
    val usuario: StateFlow<UsuarioEntity?> = _usuario

    // Carga el usuario actual desde Firebase usando UsuarioRepository
    fun cargarUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            val u = repo.getUsuario(uid)
            _usuario.value = u
        }
    }
}

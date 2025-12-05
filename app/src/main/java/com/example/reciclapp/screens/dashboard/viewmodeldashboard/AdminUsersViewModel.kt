package com.example.reciclapp.screens.dashboard.viewmodeldashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.screens.dashboard.data.local.UsuarioEntity
import com.example.reciclapp.screens.dashboard.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminUsersViewModel(
    context: Context
) : ViewModel() {

    private val repo = UsuarioRepository(context)

    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    val usuarios: StateFlow<List<UsuarioEntity>> = _usuarios

    /** Carga todos los usuarios desde Room */
    fun cargarUsuarios() {
        viewModelScope.launch {
            repo.getAllUsuarios().collect { list ->
                _usuarios.value = list
            }
        }
    }

    /** Cambia el rol de un usuario y actualiza Room + Firebase */
    fun cambiarRol(usuario: UsuarioEntity) {
        viewModelScope.launch {
            repo.updateUsuario(usuario)
            cargarUsuarios() // refresca la lista
        }
    }

    /** Filtra usuarios por búsqueda */
    fun buscarUsuarios(query: String) {
        viewModelScope.launch {
            // convertir query a minúsculas para que sea más flexible
            val q = query.lowercase()
            repo.searchUsuarios(q).collect { list ->
                _usuarios.value = list.filter {
                    it.nombres.lowercase().contains(q) ||
                            it.apellidos.lowercase().contains(q) ||
                            it.correo.lowercase().contains(q)
                }
            }
        }
    }


    /** Sincroniza usuarios desde Firebase a Room */
    fun syncUsuarios() {
        viewModelScope.launch {
            // Trae usuarios de Firebase usando la función pública
            val lista = repo.fetchUsuariosFromFirebase()

            // Limpia Room e inserta los nuevos datos
            repo.deleteAll()
            repo.insertUsuarios(lista)

            // Refresca la UI
            cargarUsuarios()
        }
    }
}
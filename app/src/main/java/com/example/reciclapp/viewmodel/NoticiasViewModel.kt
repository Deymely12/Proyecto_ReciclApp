package com.example.reciclapp.screens.noticias

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.model.Noticia
import kotlinx.coroutines.launch

class NoticiasViewModel(
    private val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _noticias = mutableStateOf<List<Noticia>>(emptyList())
    val noticias: State<List<Noticia>> = _noticias



    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading


    init {
        cargarNoticias()
    }

    fun cargarNoticias() {
        viewModelScope.launch {
            _loading.value = true
            _noticias.value = repository.getNoticias()
            _loading.value = false
        }
    }

    fun actualizarNoticias() {
        cargarNoticias()
    }
}
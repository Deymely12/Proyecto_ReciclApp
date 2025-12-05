package com.example.reciclapp.screens.puntoReciclaje

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.model.Coment
import com.example.reciclapp.model.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.apply
import kotlin.collections.any
import kotlin.collections.map
import kotlin.collections.toMutableMap

data class MarkerUIState(
    val marker: Marker,
    val expanded: Boolean = false
)

class MarkerViewModel(
    private val repository: MarkerRepository = MarkerRepository()
) : ViewModel() {

    private val _markerList = MutableStateFlow<List<MarkerUIState>>(emptyList())
    val markerList: StateFlow<List<MarkerUIState>> = _markerList

    private val _comments = MutableStateFlow<Map<String, List<Coment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Coment>>> = _comments

    init {

        loadMarkers()
    }

    fun loadMarkers() {
        viewModelScope.launch {
            repository.getAllMarkers().collect { markers ->
                _markerList.value = markers.map { MarkerUIState(it) }
            }
        }
    }

    fun toggleExpand(markerId: String) {
        _markerList.value = _markerList.value.map {
            if (it.marker.id == markerId) it.copy(expanded = !it.expanded)
            else it
        }

        // Cuando se expande, cargar comentarios
        if (_markerList.value.any { it.marker.id == markerId && it.expanded }) {
            loadComments(markerId)
        }
    }

    fun loadComments(markerId: String) {
        viewModelScope.launch {
            repository.getComments(markerId).collect { commentList ->
                _comments.value = _comments.value.toMutableMap().apply {
                    put(markerId, commentList)
                }
            }
        }
    }

    fun addComment(markerId: String, userId: String, texto: String) {
        viewModelScope.launch {
            repository.addComment(markerId, userId, texto)
        }
    }
}

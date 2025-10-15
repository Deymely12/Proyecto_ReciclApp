package com.example.reciclapp.model

import androidx.annotation.DrawableRes

data class Noticia(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    @DrawableRes val imagenRecurso: Int,
    val descripcionAdicional: String,
    val fecha: String
)

package com.example.reciclapp.screens.dashboard.data.models

import com.google.firebase.Timestamp

data class Residuo(
    val fecha: Timestamp? = null,
    val tipo: String = "",
    val imagen_url: String? = null
)
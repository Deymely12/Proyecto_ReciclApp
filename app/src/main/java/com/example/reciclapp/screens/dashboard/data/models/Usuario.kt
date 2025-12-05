package com.example.reciclapp.screens.dashboard.data.models

data class Usuario(
    val lastname: String = "",
    val firstname: String = "",
    val correo: String = "",
    val photoUrl: String? = null,
    val rol: Boolean = false,
    val uid: String = "",
    val registro: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now() // <--- nuevo campo
)


data class UsuarioData(
    val nombreCompleto: String,
    val esAdmin: Boolean
)

data class CrecimientoUsuario(
    val mes: String,
    val cantidad: Int
)

data class ResiduosData(
    val tipo: String,
    val cantidad: Int=1,
    val fecha: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)
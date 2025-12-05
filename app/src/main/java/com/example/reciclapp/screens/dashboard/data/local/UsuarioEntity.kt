package com.example.reciclapp.screens.dashboard.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val uid: String = "",   // valor por defecto
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val rol: Boolean = false            // true = admin, false = usuario
)


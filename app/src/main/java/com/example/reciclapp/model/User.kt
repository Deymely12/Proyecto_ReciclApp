package com.example.reciclapp.model

data class User(
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    //Agregue esto
    val totalPoints: Int = 0
)

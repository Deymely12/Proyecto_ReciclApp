package com.example.reciclapp.model

data class User(
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val totalPoints: Int = 0
)

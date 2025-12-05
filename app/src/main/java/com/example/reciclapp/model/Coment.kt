package com.example.reciclapp.model

import com.google.firebase.Timestamp

data class Coment(
    val id: String = "",
    val userId: String = "",
    val texto: String = "",
    val fecha: Timestamp? = null,
    val userName:String="",
    val userLastName: String= ""
)

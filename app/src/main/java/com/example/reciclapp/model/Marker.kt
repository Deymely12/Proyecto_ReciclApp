package com.example.reciclapp.model

data class Marker(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null,
    val isEnabled: Boolean = true,//state
    //Agregue esto
    val photo:String?=null,
    val direccion:String?=null
)

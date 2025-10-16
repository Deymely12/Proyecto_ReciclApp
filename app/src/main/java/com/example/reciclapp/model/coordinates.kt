package com.example.reciclapp.model
import com.google.firebase.firestore.GeoPoint

data class Coordinate(
    val name: String,
    val coordinates: GeoPoint,
    val description: String?,
    val state: Boolean?
)
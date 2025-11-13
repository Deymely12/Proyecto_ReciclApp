package com.example.pruebaclasificador.data

data class Prediction(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val confidence: Float,
    val `class`: String
)

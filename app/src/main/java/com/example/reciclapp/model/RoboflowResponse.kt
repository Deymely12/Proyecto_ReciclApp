package com.example.reciclapp.model

import com.example.pruebaclasificador.data.Prediction

data class RoboflowResponse(
    val predictions: List<Prediction>
)
package com.example.reciclapp.screens.dashboard.data.models

data class TipoResiduo(
    val name: String = "",
    val bin_color_name: String = "",
    val points_per_item: Int = 0,
    val metrics_per_item: Map<String, Double> = emptyMap()
)
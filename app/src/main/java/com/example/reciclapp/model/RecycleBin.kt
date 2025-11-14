package com.example.reciclapp.model

data class RecycleBin(
    val id: String = "",
    val name: String = "",
    val bin_color_name: String = "",
    val points_per_item: Int = 0,
    val metrics_per_item: RecycleMetrics = RecycleMetrics()
)

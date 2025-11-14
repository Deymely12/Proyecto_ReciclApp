package com.example.reciclapp.model

data class RecycleMetrics(
    val co2_avoided_kg: Double? = null,
    val co2_equivalent_avoided_kg: Double? = null,
    val waste_avoided_kg: Double? = null,
    val energy_saved_kwh: Double? = null,
    val oil_saved_liters: Double? = null,
    val trees_saved_factor: Double? = null,
    val water_saved_liters: Double? = null,
    val sand_saved_kg: Double? = null,
    val bauxite_saved_kg: Double? = null,
    val compost_produced_kg: Double? = null,
    val contamination_prevented: Double? = null
)

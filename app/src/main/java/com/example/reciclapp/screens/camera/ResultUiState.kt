package com.example.reciclapp.screens.camera

import com.example.reciclapp.model.RecycleBin
import com.example.reciclapp.model.WasteType

data class ResultUiState(
    val wasteType: WasteType? = null,
    val recycleBin: RecycleBin? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recyclingCompleted: Boolean = false
)

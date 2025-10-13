package com.example.reciclapp.Presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun news(){
    Column(){
        Text(
            text = "ReciclApp",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}
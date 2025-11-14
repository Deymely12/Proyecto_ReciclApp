package com.example.reciclapp.model

import androidx.annotation.DrawableRes
import com.example.reciclapp.R

@DrawableRes
fun getBinDrawableForDocId(docId: String): Int = when (docId) {
    "plastico" -> R.drawable.tacho_blanco
    "papel_carton" -> R.drawable.tacho_azul
    "vidrio" -> R.drawable.tacho_verde
    "metales" -> R.drawable.tacho_amarillo
    "organicos" -> R.drawable.tacho_marron
    "peligrosos" -> R.drawable.tacho_rojo
    "no_reciclables" -> R.drawable.tacho_negro
    else -> 0 // sin icono
}
package com.example.pruebaclasificador.data

import com.google.gson.annotations.SerializedName

data class ImageRequest(
    @SerializedName("image") val image: String,
    @SerializedName("confidence") val confidence: Int = 40,
    @SerializedName("overlap") val overlap: Int = 30
)

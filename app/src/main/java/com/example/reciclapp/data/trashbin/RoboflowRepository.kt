package com.example.reciclapp.data.trashbin

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.reciclapp.data.trashbin.RoboflowApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RoboflowRepository(

    private val api: RoboflowApi = defaultRoboflowApi()
) {

    companion object {
        private const val API_KEY = "ZGM3ubM5BigbK8TDTYZc"
        private const val MODEL_ENDPOINT = "trash-bin-asn0s/1"

        private fun defaultRoboflowApi(): RoboflowApi {
            val client = OkHttpClient.Builder()
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://serverless.roboflow.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(RoboflowApi::class.java)
        }
    }

    /**
     * Lee la imagen desde el Uri, la codifica en Base64
     * y la envía al modelo de Roboflow usando Retrofit.
     */
    suspend fun analyzeImage(
        context: Context,
        imageUri: Uri
    ): String = withContext(Dispatchers.IO) {

        // 1. Leer bytes de la imagen
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalStateException("No se pudo leer la imagen")

        // 2. Codificar en Base64 (sin saltos de línea)
        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        // 3. Llamada Retrofit (suspend)
        api.detect(
            modelEndpoint = MODEL_ENDPOINT,
            apiKey = API_KEY,
            name = "image.jpg",
            imageBase64 = base64Image
        )
    }
}
package com.example.pruebaclasificador

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaclasificador.data.ImageRequest
import com.example.pruebaclasificador.data.Prediction
import com.example.pruebaclasificador.data.RoboflowResponse
import com.example.pruebaclasificador.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import com.example.pruebaclasificador.data.ClassMapping

class WasteViewModel : ViewModel() {

    var classification: String? = null
    var predictions: List<Prediction>? = null

    fun classifyImage(bitmap: Bitmap, onResult: (List<Prediction>?) -> Unit) {
        viewModelScope.launch {
            try {
                // Redimensionar imagen
                val resizedBitmap = resizeBitmap(bitmap, 640)
                Log.d("WasteViewModel", "Imagen redimensionada a: ${resizedBitmap.width}x${resizedBitmap.height}")

                // Convertir bitmap a archivo temporal
                val file = bitmapToFile(resizedBitmap)
                Log.d("WasteViewModel", "Archivo creado, tamaño: ${file.length()} bytes")

                // Crear RequestBody y MultipartBody.Part
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Llamar al API con Multipart
                val response = RetrofitClient.api.classifyImageMultipart(
                    RetrofitClient.MODEL_ID,
                    RetrofitClient.API_KEY,
                    body
                )

                // Eliminar archivo temporal
                file.delete()

                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d("WasteViewModel", "Respuesta: $responseBody")

                    val gson = Gson()
                    val roboflowResponse = gson.fromJson(responseBody, RoboflowResponse::class.java)

                    predictions = roboflowResponse.predictions
                    classification = if (roboflowResponse.predictions.isNotEmpty()) {
                        val topPrediction = roboflowResponse.predictions.maxByOrNull { it.confidence }
                        val originalClass = topPrediction?.`class` ?: "Desconocido"
                        val mappedCategory = ClassMapping.mapping[originalClass] ?: "Otros residuos"
                        //"Clase: ${topPrediction?.`class`}\nConfianza: ${(topPrediction?.confidence?.times(100))?.toInt()}%"
                        "Categoría: $mappedCategory\n(Detectado: $originalClass)\nConfianza: ${(topPrediction?.confidence?.times(100))?.toInt()}%"


                    } else {
                        "No se detectaron objetos"
                    }

                    onResult(roboflowResponse.predictions)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("WasteViewModel", "Error HTTP ${response.code()}: $errorBody")
                    onResult(null)
                }

            } catch (e: Exception) {
                Log.e("WasteViewModel", " Error: ${e.message}", e)
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File.createTempFile("upload_image_", ".jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }
}
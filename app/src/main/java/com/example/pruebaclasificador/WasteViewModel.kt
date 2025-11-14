package com.example.pruebaclasificador

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaclasificador.data.Prediction
import com.example.pruebaclasificador.data.RoboflowResponse
import com.example.pruebaclasificador.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.example.pruebaclasificador.data.ClassMapping
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class WasteViewModel : ViewModel() {


    var classification by mutableStateOf<String?>(null)
        private set

    var topPrediction by mutableStateOf<Prediction?>(null)
        private set

    var categoria by mutableStateOf<String?>(null)
    private set

    fun classifyImage(bitmap: Bitmap, onResult: (Prediction?) -> Unit) {
        viewModelScope.launch {
            try {

                val bitMapRedimensionado = resizeBitmap(bitmap, 640)
                //Log.d("WasteViewModel", "Imagen redimensionada a: ${bitMapRedimensionado.width}x${bitMapRedimensionado.height}")


                val archivo = bitmapToFile(bitMapRedimensionado)
                //Log.d("WasteViewModel", "Archivo creado, tamaño: ${archivo.length()} bytes")

                val archivoRequest = archivo.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val cuerpoArchivo = MultipartBody.Part.createFormData("file", archivo.name, archivoRequest)

                val respuesta = RetrofitClient.api.classifyImageMultipart(
                    RetrofitClient.MODEL_ID,
                    RetrofitClient.API_KEY,
                    cuerpoArchivo
                )

                archivo.delete()

                if (respuesta.isSuccessful) {
                    val responseBody = respuesta.body()?.string()
                    Log.d("WasteViewModel", "Respuesta: $responseBody")

                    val gson = Gson()
                    val respuestaRoboflow = gson.fromJson(responseBody, RoboflowResponse::class.java)

                    topPrediction = respuestaRoboflow.predictions.maxByOrNull { it.confidence }

                    if(topPrediction!=null){
                        val mappedCategory = ClassMapping.mapping[topPrediction?.`class`] ?: "Otros residuos"
                        classification =

                         "Categoría: $mappedCategory\n" +
                                "(Clase Original: ${topPrediction?.`class`})\n" +
                                "Confianza: ${(topPrediction?.confidence?.times(100))?.toInt()}%"

                        categoria=mappedCategory

                    } else {
                       classification= "No se detectaron objetos"
                    }

                    onResult(topPrediction)
                } else {
                    val errorBody = respuesta.errorBody()?.string()
                    Log.e("WasteViewModel", "Error HTTP ${respuesta.code()}: $errorBody")
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
        val ancho = bitmap.width
        val alto = bitmap.height

        if (ancho <= maxSize && alto <= maxSize) {
            return bitmap
        }

        val ratio = if (ancho > alto) {
            maxSize.toFloat() / ancho
        } else {
            maxSize.toFloat() / alto
        }

        val nuevoAncho = (ancho * ratio).toInt()
        val nuevoAlto = (alto * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, nuevoAncho, nuevoAlto, true)
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val archivo = File.createTempFile("upload_image_", ".jpg")
        val flujoSalida = FileOutputStream(archivo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, flujoSalida)
        flujoSalida.flush()
        flujoSalida.close()
        return archivo
    }
}
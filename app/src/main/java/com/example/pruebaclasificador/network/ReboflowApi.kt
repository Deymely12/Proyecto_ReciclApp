package com.example.pruebaclasificador.network


import com.example.pruebaclasificador.data.ImageRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReboflowApi {
    // Nueva función con Multipart
    @Multipart
    @POST("{modelId}")
    suspend fun classifyImageMultipart(
        @Path("modelId", encoded = true) modelId: String,
        @Query("api_key") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    /*
    // Mantener la anterior por si acaso
    @Headers("Content-Type: application/json")
    @POST("{modelId}")
    suspend fun classifyImage(
        @Path("modelId", encoded = true) modelId: String,
        @Query("api_key") apiKey: String,
        @Body request: ImageRequest
    ): Response<ResponseBody>

     */

}

/*
interface ReboflowApi {
    @POST
    fun classifyImage(
        @Url url: String,
        @Body imageRequest: ImageRequest
    ): Call<RoboflowResponse>
}
*/
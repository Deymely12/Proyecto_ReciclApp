package com.example.pruebaclasificador.network


import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ReboflowApi {
    @Multipart
    @POST("{modelId}")
    suspend fun classifyImageMultipart(
        @Path("modelId", encoded = true) modelId: String,
        @Query("api_key") apiKey: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

}

package com.example.reciclapp.data.trashbin

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoboflowApi {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("{modelEndpoint}")
    suspend fun detect(
        @Path("modelEndpoint") modelEndpoint: String,
        @Query("api_key") apiKey: String,
        @Query("name") name: String = "image.jpg",
        @Body imageBase64: String
    ): String
}
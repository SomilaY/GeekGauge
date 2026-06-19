package com.somila.geekgauge.data.remote

import com.somila.geekgauge.data.gemini.GeminiRequest
import com.somila.geekgauge.data.gemini.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateReport(
        @Path("model") modelName: String, // Pass the model dynamically
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
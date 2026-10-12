package com.example.easystock2.ai.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {
    // Usamos la versión estable (v1) y el alias "latest" para asegurar la compatibilidad
    @POST("https://generativelanguage.googleapis.com/v1/models/gemini-3.8-flash-latest:generateContent")
    suspend fun generateRecommendations(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
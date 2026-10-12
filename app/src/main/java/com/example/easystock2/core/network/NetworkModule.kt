package com.example.easystock2.core.network

import com.example.easystock2.ai.data.GeminiApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Configuramos el parser para que ignore campos extra de la respuesta de Google
    private val json = Json { ignoreUnknownKeys = true }

    val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Usamos MediaType.get() compatible con OkHttp 3
            .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
            .build()
            .create(GeminiApi::class.java)
    }
}
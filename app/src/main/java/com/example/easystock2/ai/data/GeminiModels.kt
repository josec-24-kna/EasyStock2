package com.example.easystock2.ai.data

import kotlinx.serialization.Serializable

// --- MODELOS PARA LA PETICIÓN (REQUEST) ---
@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

// --- MODELOS PARA LA RESPUESTA (RESPONSE) ---
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)
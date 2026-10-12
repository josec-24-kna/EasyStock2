package com.example.easystock2.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easystock2.ai.data.Content
import com.example.easystock2.ai.data.GeminiRequest
import com.example.easystock2.ai.data.Part
import com.example.easystock2.core.network.NetworkModule
import com.example.easystock2.inventory.data.InventoryDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiViewModel(
    private val inventoryDao: InventoryDao,
    private val userId: Int
) : ViewModel() {

    // IMPORTANTE: Pega tu API Key de Google AI Studio aquí
    private val API_KEY = "TU_LLAVE_AQUI"

    private val _recommendation = MutableStateFlow<String?>(null)
    val recommendation: StateFlow<String?> = _recommendation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generateRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 1. Extraemos el stock actual de Room
                val stock = inventoryDao.getProductsByUser(userId).first()

                if (stock.isEmpty()) {
                    _recommendation.value = "Tu inventario está vacío. Escanea productos primero."
                    _isLoading.value = false
                    return@launch
                }

                // 2. Convertimos la lista de productos a un texto legible para la IA
                val stockSummary = stock.joinToString(separator = "\n") {
                    "- ${it.cantidad}x ${it.tipo} ${it.modelo} (${it.color}) a $${it.precio}"
                }

                // 3. Diseñamos el Prompt (Instrucciones para Gemini)
                val prompt = """
                    Eres un experto consultor de retail y análisis de inventario. 
                    Analiza mi stock actual y cruzalo con tendencias generales de mercado. 
                    Dame 3 sugerencias precisas en formato de viñetas:
                    1. ¿Qué artículos tienen riesgo de estancarse y debería poner en descuento?
                    2. ¿Qué artículos similares o accesorios complementarios están en tendencia y debería comprar para ampliar mi catálogo?
                    3. Una estrategia rápida para mejorar mi rotación de inventario.
                    
                    Aquí está mi inventario actual:
                    $stockSummary
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                // 4. Hacemos la petición HTTP a la API
                val response = NetworkModule.geminiApi.generateRecommendations(API_KEY, request)

                // 5. Extraemos el texto de la respuesta JSON
                val aiText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                _recommendation.value = aiText ?: "No se pudo generar una respuesta clara."

            } catch (e: retrofit2.HttpException) {
                // Esto extraerá el mensaje exacto que Google nos está enviando
                val errorBody = e.response()?.errorBody()?.string()
                _error.value = "Error de Google HTTP ${e.code()}:\n$errorBody"
            } catch (e: Exception) {
                _error.value = "Error local: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class AiViewModelFactory(
    private val inventoryDao: InventoryDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiViewModel(inventoryDao, userId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
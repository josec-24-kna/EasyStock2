package com.example.easystock2.inventory.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easystock2.inventory.data.InventoryDao
import com.example.easystock2.inventory.data.ProductEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Estructura esperada en el código QR
@Serializable
data class QrProductData(
    val modelo: String,
    val color: String,
    val ubicacion: String,
    val tipo: String,
    val cantidad: Int,
    val precio: Double
)

class InventoryViewModel(
    private val inventoryDao: InventoryDao,
    private val userId: Int // Se requiere para guardar el producto al usuario activo
) : ViewModel() {

    // Flujo reactivo: la UI se actualizará automáticamente si cambia la tabla
    val products: StateFlow<List<ProductEntity>> = inventoryDao.getProductsByUser(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun processScannedQr(qrData: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val format = Json { ignoreUnknownKeys = true }
                val parsedData = format.decodeFromString<QrProductData>(qrData)

                val newProduct = ProductEntity(
                    userId = userId,
                    modelo = parsedData.modelo,
                    color = parsedData.color,
                    ubicacion = parsedData.ubicacion,
                    tipo = parsedData.tipo,
                    cantidad = parsedData.cantidad,
                    precio = parsedData.precio
                )

                inventoryDao.insertProduct(newProduct)
                onSuccess()
            } catch (e: Exception) {
                // Ahora sabremos si la cámara lee el QR pero el JSON está roto
                onError("Error JSON: ${e.localizedMessage}")
            }
        }
    }
}

class InventoryViewModelFactory(
    private val inventoryDao: InventoryDao,
    private val userId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(inventoryDao, userId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
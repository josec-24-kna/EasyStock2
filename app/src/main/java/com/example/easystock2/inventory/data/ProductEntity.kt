package com.example.easystock2.inventory.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.easystock2.auth.data.UserEntity

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("userId"),
            onDelete = ForeignKey.CASCADE // Si se borra el usuario, se borra su inventario
        )
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val productId: Int = 0,
    val userId: Int, // ID del usuario dueño de este inventario
    val modelo: String, // ej. iPhone 15, ThinkPad P16s
    val color: String, // ej. Rojo, Negro
    val ubicacion: String, // ej. Pasillo 3, Estante B
    val tipo: String, // ej. Funda, Cargador GaN, Cristal Templado
    val cantidad: Int,
    val precio: Double
)
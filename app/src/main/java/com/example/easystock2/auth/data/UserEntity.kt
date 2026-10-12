package com.example.easystock2.auth.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val pin: String // Para hacerlo práctico, usaremos un PIN o contraseña simple
)
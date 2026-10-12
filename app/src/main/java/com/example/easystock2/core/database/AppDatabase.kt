package com.example.easystock2.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.easystock2.auth.data.UserDao
import com.example.easystock2.auth.data.UserEntity
import com.example.easystock2.inventory.data.InventoryDao
import com.example.easystock2.inventory.data.ProductEntity

// 1. Agregamos ProductEntity y subimos la versión a 2
@Database(entities = [UserEntity::class, ProductEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    // 2. Declaramos el nuevo DAO
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_ia_db"
                )
                    .fallbackToDestructiveMigration() // 3. Evita crasheos al modificar tablas en desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.example.easystock2.inventory.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("SELECT * FROM products WHERE userId = :userId")
    fun getProductsByUser(userId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Update
    suspend fun updateProduct(product: ProductEntity): Int

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: Int): Int
}
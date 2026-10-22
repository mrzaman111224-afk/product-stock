package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE status != 'ARCHIVED' ORDER BY name ASC")
    fun observeActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?

    @Query("SELECT COUNT(*) FROM products WHERE status != 'ARCHIVED'")
    suspend fun getActiveProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stock = :newStock, status = :status, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int, status: ProductStatus, updatedAt: Long)

    @Query("UPDATE products SET status = 'ARCHIVED', updatedAt = :updatedAt WHERE id = :productId")
    suspend fun softDelete(productId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun hardDelete(productId: String)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}

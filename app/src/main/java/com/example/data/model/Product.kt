package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductStatus {
    ACTIVE,
    ARCHIVED,
    OUT_OF_STOCK
}

@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val id: String,
    val sku: String,
    val name: String,
    val category: String,
    val description: String = "",
    val purchaseCost: Double,
    val sellingPrice: Double,
    val stock: Int,
    val lowStockThreshold: Int = 5,
    val barcode: String = "",
    val status: ProductStatus = ProductStatus.ACTIVE,
    val createdBy: String,
    val createdByName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

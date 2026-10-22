package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InventoryChangeType {
    RESTOCK,
    SALE,
    ADJUSTMENT,
    DAMAGED,
    RETURN
}

@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productName: String,
    val sku: String,
    val changeType: InventoryChangeType,
    val quantityDelta: Int,
    val previousStock: Int,
    val newStock: Int,
    val performedBy: String,
    val performedByName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String = ""
)

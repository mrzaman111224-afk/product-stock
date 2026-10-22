package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH,
    CARD,
    MFS_BKASH,
    MFS_NAGAD,
    BANK_TRANSFER
}

data class CartItem(
    val productId: String,
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey
    val id: String,
    val invoiceNumber: String,
    val cashierId: String,
    val cashierName: String,
    val itemsJson: String,
    val itemCount: Int,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val timestamp: Long = System.currentTimeMillis(),
    val customerNote: String = ""
)

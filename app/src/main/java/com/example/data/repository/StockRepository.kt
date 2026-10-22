package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.InventoryChangeType
import com.example.data.model.InventoryLog
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.StoreSetting
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DashboardMetrics(
    val totalActiveProducts: Int = 0,
    val totalStockUnits: Int = 0,
    val totalInventoryValue: Double = 0.0,
    val todaySalesTotal: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

class StockRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()
    private val logDao = database.inventoryLogDao()
    private val settingDao = database.storeSettingDao()

    fun observeActiveProducts(): Flow<List<Product>> = productDao.observeActiveProducts()
    fun observeAllProducts(): Flow<List<Product>> = productDao.observeAllProducts()
    fun observeAllTransactions(): Flow<List<TransactionRecord>> = transactionDao.observeAllTransactions()
    fun observeAllLogs(): Flow<List<InventoryLog>> = logDao.observeAllLogs()
    fun observeAllSettings(): Flow<List<StoreSetting>> = settingDao.observeAllSettings()

    suspend fun getSetting(key: String): String? = settingDao.getSetting(key)
    suspend fun setSetting(key: String, value: String) = settingDao.setSetting(StoreSetting(key, value))

    /**
     * Executes atomic sale in a single database transaction.
     * Prevents race conditions, verifies stock, updates product counts,
     * logs inventory changes, and records the customer transaction.
     */
    suspend fun executeAtomicSale(
        cashier: UserAccount,
        cart: List<CartItem>,
        taxRatePercent: Double,
        discount: Double,
        paymentMethod: PaymentMethod,
        customerNote: String = ""
    ): Result<TransactionRecord> {
        if (cart.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart is empty."))
        }

        return try {
            val transactionRecord = database.withTransaction {
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
                val randSuffix = (1000..9999).random()
                val invoiceNumber = "INV-$dateStr-$randSuffix"

                // 1. Verify stock and calculate
                var calculatedSubtotal = 0.0
                for (item in cart) {
                    val current = productDao.getProductById(item.productId)
                        ?: throw IllegalStateException("Product ${item.productName} not found.")

                    if (current.stock < item.quantity) {
                        throw IllegalStateException(
                            "Insufficient stock for '${current.name}'. Available: ${current.stock}, Requested: ${item.quantity}"
                        )
                    }

                    val newStock = current.stock - item.quantity
                    val newStatus = if (newStock == 0) ProductStatus.OUT_OF_STOCK else current.status

                    // Update product stock atomically
                    productDao.updateStock(
                        productId = current.id,
                        newStock = newStock,
                        status = newStatus,
                        updatedAt = now
                    )

                    // Write inventory audit log
                    logDao.insertLog(
                        InventoryLog(
                            id = UUID.randomUUID().toString(),
                            productId = current.id,
                            productName = current.name,
                            sku = current.sku,
                            changeType = InventoryChangeType.SALE,
                            quantityDelta = -item.quantity,
                            previousStock = current.stock,
                            newStock = newStock,
                            performedBy = cashier.id,
                            performedByName = cashier.displayName,
                            timestamp = now,
                            reason = "Invoice #$invoiceNumber sale checkout"
                        )
                    )

                    calculatedSubtotal += item.unitPrice * item.quantity
                }

                val tax = (calculatedSubtotal * (taxRatePercent / 100.0))
                val totalAmount = (calculatedSubtotal + tax - discount).coerceAtLeast(0.0)

                // Simple JSON representation of items
                val itemsSummary = cart.joinToString(separator = "; ") {
                    "${it.productName} (x${it.quantity} @ ৳${it.unitPrice}) = ৳${it.subtotal}"
                }

                val record = TransactionRecord(
                    id = UUID.randomUUID().toString(),
                    invoiceNumber = invoiceNumber,
                    cashierId = cashier.id,
                    cashierName = cashier.displayName,
                    itemsJson = itemsSummary,
                    itemCount = cart.sumOf { it.quantity },
                    subtotal = calculatedSubtotal,
                    tax = tax,
                    discount = discount,
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    timestamp = now,
                    customerNote = customerNote
                )

                transactionDao.insertTransaction(record)
                record
            }
            Result.success(transactionRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Manual Stock Calibration or Restock with verified Audit Trail
     */
    suspend fun adjustStock(
        productId: String,
        newStock: Int,
        changeType: InventoryChangeType,
        user: UserAccount,
        reason: String
    ): Result<Unit> {
        if (newStock < 0) return Result.failure(IllegalArgumentException("Stock cannot be negative."))

        return try {
            database.withTransaction {
                val current = productDao.getProductById(productId)
                    ?: throw IllegalStateException("Product not found.")

                val delta = newStock - current.stock
                val now = System.currentTimeMillis()
                val status = if (newStock == 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE

                productDao.updateStock(productId, newStock, status, now)

                logDao.insertLog(
                    InventoryLog(
                        id = UUID.randomUUID().toString(),
                        productId = current.id,
                        productName = current.name,
                        sku = current.sku,
                        changeType = changeType,
                        quantityDelta = delta,
                        previousStock = current.stock,
                        newStock = newStock,
                        performedBy = user.id,
                        performedByName = user.displayName,
                        timestamp = now,
                        reason = reason.ifBlank { "Manual adjustment" }
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addProduct(product: Product): Result<Unit> {
        val existing = productDao.getProductBySku(product.sku)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("SKU '${product.sku}' already exists."))
        }
        productDao.insertProduct(product)
        logDao.insertLog(
            InventoryLog(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                productName = product.name,
                sku = product.sku,
                changeType = InventoryChangeType.RESTOCK,
                quantityDelta = product.stock,
                previousStock = 0,
                newStock = product.stock,
                performedBy = product.createdBy,
                performedByName = product.createdByName,
                reason = "New product registration"
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    suspend fun deleteProduct(productId: String, requesterRole: UserRole): Result<Unit> {
        if (requesterRole != UserRole.ADMIN) {
            return Result.failure(SecurityException("Unauthorized: Only Admin users can delete products."))
        }
        productDao.softDelete(productId)
        return Result.success(Unit)
    }

    suspend fun clearAllStoreData(): Result<Unit> {
        return try {
            database.withTransaction {
                productDao.clearAll()
                transactionDao.clearAll()
                logDao.clearAll()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportCsv(products: List<Product>): String {
        val header = "SKU,Name,Category,Cost,Price,Stock,Threshold,Status\n"
        val rows = products.joinToString("\n") { p ->
            "\"${p.sku}\",\"${p.name.replace("\"", "\"\"")}\",\"${p.category}\",${p.purchaseCost},${p.sellingPrice},${p.stock},${p.lowStockThreshold},\"${p.status}\""
        }
        return header + rows
    }
}

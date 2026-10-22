package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.AuthRepository
import com.example.data.repository.DashboardMetrics
import com.example.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

sealed interface NavigationDestination {
    object Dashboard : NavigationDestination
    object Products : NavigationDestination
    object POS : NavigationDestination
    object Transactions : NavigationDestination
    object Logs : NavigationDestination
    object Staff : NavigationDestination
    object Settings : NavigationDestination
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val authRepo = AuthRepository(database.userDao(), database.storeSettingDao())
    val stockRepo = StockRepository(database)

    val currentUser = authRepo.currentUser
    val allUsers = authRepo.observeAllUsers().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val activeProducts = stockRepo.observeActiveProducts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allTransactions = stockRepo.observeAllTransactions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allLogs = stockRepo.observeAllLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allSettings = stockRepo.observeAllSettings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Navigation & UI State
    private val _currentDestination = MutableStateFlow<NavigationDestination>(NavigationDestination.Dashboard)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // POS Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val paymentMethod: StateFlow<PaymentMethod> = _paymentMethod.asStateFlow()

    private val _completedTransaction = MutableStateFlow<TransactionRecord?>(null)
    val completedTransaction: StateFlow<TransactionRecord?> = _completedTransaction.asStateFlow()

    private val _isProcessingSale = MutableStateFlow(false)
    val isProcessingSale: StateFlow<Boolean> = _isProcessingSale.asStateFlow()

    // Computed Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        activeProducts,
        allTransactions
    ) { products, transactions ->
        val totalUnits = products.sumOf { it.stock }
        val totalVal = products.sumOf { it.stock * it.purchaseCost }
        val lowStock = products.count { it.stock in 1..it.lowStockThreshold }
        val outOfStock = products.count { it.stock == 0 }

        val startOfDayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = startOfDayCal.timeInMillis
        val todayTxs = transactions.filter { it.timestamp >= startOfDay }
        val todayTotal = todayTxs.sumOf { it.totalAmount }

        DashboardMetrics(
            totalActiveProducts = products.size,
            totalStockUnits = totalUnits,
            totalInventoryValue = totalVal,
            todaySalesTotal = todayTotal,
            todayOrdersCount = todayTxs.size,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardMetrics()
    )

    fun navigateTo(dest: NavigationDestination) {
        _currentDestination.value = dest
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    // Cart Operations
    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            showMessage("${product.name} is out of stock.")
            return
        }

        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            if (existing.quantity >= product.stock) {
                showMessage("Maximum available stock (${product.stock}) reached.")
                return
            }
            val newQty = existing.quantity + 1
            currentList[index] = existing.copy(
                quantity = newQty,
                subtotal = newQty * existing.unitPrice
            )
        } else {
            currentList.add(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    sku = product.sku,
                    quantity = 1,
                    unitPrice = product.sellingPrice,
                    subtotal = product.sellingPrice
                )
            )
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = currentList[index]
            val product = activeProducts.value.find { it.id == productId }
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else {
                if (product != null && newQty > product.stock) {
                    showMessage("Cannot exceed stock (${product.stock})")
                    return
                }
                currentList[index] = item.copy(
                    quantity = newQty,
                    subtotal = newQty * item.unitPrice
                )
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
        _discount.value = 0.0
    }

    fun setDiscount(amount: Double) {
        _discount.value = amount.coerceAtLeast(0.0)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _paymentMethod.value = method
    }

    fun checkoutSale() {
        val user = currentUser.value
        if (user == null) {
            showMessage("Please sign in to checkout.")
            return
        }
        if (_cart.value.isEmpty()) {
            showMessage("Cart is empty.")
            return
        }

        viewModelScope.launch {
            _isProcessingSale.value = true
            val result = stockRepo.executeAtomicSale(
                cashier = user,
                cart = _cart.value,
                taxRatePercent = 0.0,
                discount = _discount.value,
                paymentMethod = _paymentMethod.value
            )
            _isProcessingSale.value = false

            result.onSuccess { txRecord ->
                _completedTransaction.value = txRecord
                clearCart()
                showMessage("Sale completed successfully! Invoice #${txRecord.invoiceNumber}")
            }.onFailure { err ->
                showMessage("Checkout failed: ${err.message}")
            }
        }
    }

    fun dismissReceipt() {
        _completedTransaction.value = null
    }

    // Product Management Operations
    fun saveProduct(
        id: String?,
        sku: String,
        name: String,
        category: String,
        description: String,
        cost: Double,
        price: Double,
        stock: Int,
        threshold: Int,
        onDone: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            if (id == null) {
                val newProd = Product(
                    id = UUID.randomUUID().toString(),
                    sku = sku.trim().uppercase(),
                    name = name.trim(),
                    category = category.trim().ifBlank { "General" },
                    description = description.trim(),
                    purchaseCost = cost,
                    sellingPrice = price,
                    stock = stock,
                    lowStockThreshold = threshold,
                    status = if (stock == 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                )
                val res = stockRepo.addProduct(newProd)
                res.onSuccess {
                    showMessage("Product '${newProd.name}' created.")
                    onDone()
                }.onFailure { showMessage(it.message ?: "Failed to add product") }
            } else {
                val existing = activeProducts.value.find { it.id == id }
                if (existing != null) {
                    val updated = existing.copy(
                        name = name.trim(),
                        category = category.trim().ifBlank { "General" },
                        description = description.trim(),
                        purchaseCost = cost,
                        sellingPrice = price,
                        stock = stock,
                        lowStockThreshold = threshold,
                        status = if (stock == 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE
                    )
                    stockRepo.updateProduct(updated)
                    showMessage("Product updated.")
                    onDone()
                }
            }
        }
    }

    fun deleteProduct(productId: String) {
        val user = currentUser.value ?: return
        if (user.role != UserRole.ADMIN) {
            showMessage("Staff cannot delete products. Admin permission required.")
            return
        }
        viewModelScope.launch {
            val res = stockRepo.deleteProduct(productId, user.role)
            res.onSuccess { showMessage("Product deleted.") }
                .onFailure { showMessage(it.message ?: "Delete failed") }
        }
    }

    // Stock Adjustment
    fun adjustStock(
        productId: String,
        newStock: Int,
        changeType: InventoryChangeType,
        reason: String,
        onDone: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = stockRepo.adjustStock(
                productId = productId,
                newStock = newStock,
                changeType = changeType,
                user = user,
                reason = reason
            )
            res.onSuccess {
                showMessage("Stock adjusted successfully.")
                onDone()
            }.onFailure {
                showMessage("Adjustment failed: ${it.message}")
            }
        }
    }

    // Staff Management
    fun approveStaff(userId: String) {
        viewModelScope.launch {
            authRepo.approveStaff(userId)
            showMessage("Staff account approved.")
        }
    }

    fun rejectStaff(userId: String) {
        viewModelScope.launch {
            authRepo.rejectStaff(userId)
            showMessage("Staff account request rejected.")
        }
    }

    fun suspendStaff(userId: String) {
        viewModelScope.launch {
            authRepo.suspendStaff(userId)
            showMessage("Staff account suspended.")
        }
    }

    fun changeRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            val res = authRepo.changeUserRole(userId, role)
            res.onSuccess {
                showMessage("User role updated to ${role.name}.")
            }.onFailure {
                showMessage(it.message ?: "Failed to update role")
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val res = authRepo.deleteUser(userId)
            res.onSuccess {
                showMessage("User account deleted.")
            }.onFailure {
                showMessage(it.message ?: "Delete failed")
            }
        }
    }

    fun saveSetting(key: String, value: String) {
        viewModelScope.launch {
            stockRepo.setSetting(key, value)
            showMessage("Settings saved.")
        }
    }

    fun updateAdminPassword(oldPass: String, newPass: String, onDone: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val res = authRepo.updatePassword(user.id, oldPass, newPass)
            res.onSuccess {
                showMessage("Admin password updated successfully.")
                onDone()
            }.onFailure {
                showMessage(it.message ?: "Failed to update password")
            }
        }
    }

    fun createStaffDirectly(
        email: String,
        displayName: String,
        passwordRaw: String,
        role: UserRole,
        onDone: () -> Unit
    ) {
        val user = currentUser.value ?: return
        if (user.role != UserRole.ADMIN) {
            showMessage("Only Administrator can create accounts.")
            return
        }
        viewModelScope.launch {
            val res = authRepo.createStaffDirectly(email, displayName, passwordRaw, role)
            res.onSuccess {
                showMessage("Account created for ${it.displayName}.")
                onDone()
            }.onFailure {
                showMessage(it.message ?: "Failed to create account")
            }
        }
    }

    fun resetAllStoreData() {
        val user = currentUser.value ?: return
        if (user.role != UserRole.ADMIN) {
            showMessage("Only Administrator can reset store data.")
            return
        }
        viewModelScope.launch {
            val res = stockRepo.clearAllStoreData()
            res.onSuccess {
                clearCart()
                showMessage("All store products, stock, and sales reset to 0.")
            }.onFailure {
                showMessage("Reset failed: ${it.message}")
            }
        }
    }

    fun logout() {
        authRepo.logout()
        clearCart()
        _currentDestination.value = NavigationDestination.Dashboard
    }
}

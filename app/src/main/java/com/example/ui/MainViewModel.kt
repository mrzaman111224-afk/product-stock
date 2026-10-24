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
import kotlinx.coroutines.Dispatchers
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
    val authRepo = AuthRepository(database.userDao(), database.storeSettingDao(), application)
    val stockRepo = StockRepository(database)

    val currentUser = authRepo.currentUser
    val isSessionRestored = authRepo.isSessionRestored
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

    init {
        viewModelScope.launch {
            authRepo.restoreSession()
        }
    }

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

    private val _selectedCategoryFilter = MutableStateFlow<String>("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    fun selectCategory(cat: String) {
        _selectedCategoryFilter.value = cat
    }

    fun navigateToCategory(cat: String) {
        _selectedCategoryFilter.value = cat
        _currentDestination.value = NavigationDestination.Products
    }

    fun navigateTo(dest: NavigationDestination) {
        _currentDestination.value = dest
    }

    fun showMessage(msg: String) {
        _uiMessage.value = msg
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun quickAdjustStock(productId: String, delta: Int, reason: String = "Manual Adjustment") {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val prod = activeProducts.value.find { it.id == productId } ?: return@launch
            val newStock = (prod.stock + delta).coerceAtLeast(0)
            val changeType = if (delta >= 0) InventoryChangeType.RESTOCK else InventoryChangeType.DAMAGED
            val result = stockRepo.adjustStock(
                productId = productId,
                newStock = newStock,
                changeType = changeType,
                user = user,
                reason = reason
            )
            result.onSuccess {
                showMessage("Stock updated for '${prod.name}' ($delta units -> New Stock: $newStock)")
            }.onFailure {
                showMessage(it.message ?: "Failed to adjust stock")
            }
        }
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

    /**
     * Seeds the authentic Moner Upohar boutique jewelry catalog (6 categories requested by user)
     */
    fun seedMonerUpoharCatalog() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val items = listOf(
                // 1. Necklace (নেকলেস / গলার হার)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-NCK-001",
                    name = "Clover Gold Pendant Necklace (ক্লোভার গোল্ড নেকলেস)",
                    category = "Necklace",
                    description = "প্রিমিয়াম ১৮ ক্যারেট গোল্ড প্লেটিং এবং ফোর-লিফ ক্লোভার পেন্ডেন্ট নেকলেস। লাক্সারি ভেলভেট কুশন লুক।",
                    purchaseCost = 950.0,
                    sellingPrice = 1550.0,
                    stock = 25,
                    lowStockThreshold = 5,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-NCK-002",
                    name = "Royal Kundan Choker Necklace (রয়্যাল কুন্দন চোকার হার)",
                    category = "Necklace",
                    description = "মার্জিত ব্রাইডাল কুন্দন স্টোন ও পার্ল পেন্ডেন্ট হার। উৎসব ও বিয়ের জন্য রাজকীয় চয়েস।",
                    purchaseCost = 1250.0,
                    sellingPrice = 1950.0,
                    stock = 20,
                    lowStockThreshold = 5,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                // 2. Bracelet (ব্রেসলেট)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-BRC-001",
                    name = "Golden Multi-Band Bangle Bracelet (ট্রিপল ব্যান্ড গোল্ডেন ব্রেসলেট)",
                    category = "Bracelet",
                    description = "প্রিমিয়াম গোল্ডেন স্ফিয়ার বিডস ও ট্রিপল-ব্যান্ড ওপেনিং লাক্সারি ব্রেসলেট।",
                    purchaseCost = 750.0,
                    sellingPrice = 1350.0,
                    stock = 35,
                    lowStockThreshold = 8,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-BRC-002",
                    name = "Adjustable Crystal Tennis Bracelet (ক্রিস্টাল টেনিস ব্রেসলেট)",
                    category = "Bracelet",
                    description = "ঝলমলে সুইস জিরকন খচিত এডজাস্টেবল টেনিস ব্রেসলেট। যেকোনো সাইজের হাতে চমৎকার মানায়।",
                    purchaseCost = 550.0,
                    sellingPrice = 950.0,
                    stock = 40,
                    lowStockThreshold = 6,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                // 3. Kashmiri Churi (কাশ্মীরি চুড়ি)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-CHR-001",
                    name = "Velvet Red Bridal Kashmiri Churi Set (ভেলভেট ব্রাইডাল কাশ্মীরি চুড়ি সেট)",
                    category = "Kashmiri Churi",
                    description = "রয়্যাল লাল ভেলভেট, গোল্ডেন ঘুঙুর ও ঝকঝকে স্টোন ওয়ার্ক চুড়ি সেট। স্পেশাল জুয়েলারি বক্সে সংরক্ষিত।",
                    purchaseCost = 850.0,
                    sellingPrice = 1450.0,
                    stock = 30,
                    lowStockThreshold = 6,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-CHR-002",
                    name = "Stone Work Silk Kashmiri Churi (স্টোন ওয়ার্ক কাশ্মীরি চুড়ি)",
                    category = "Kashmiri Churi",
                    description = "জারকনের ঝিলিক ও রাজকীয় ঐতিহ্যবাহী কাশ্মীরি সিল্ক স্টোন ওয়ার্ক চুড়ি।",
                    purchaseCost = 980.0,
                    sellingPrice = 1650.0,
                    stock = 25,
                    lowStockThreshold = 4,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                // 4. Finger Ring (আংটি)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-RNG-001",
                    name = "Crystal Leaf Adjustable Gold Ring (ক্রিস্টাল লিফ গোল্ডেন আংটি)",
                    category = "Finger Ring",
                    description = "ঝলমলে মার্কুইস জিরকন পাতা ও গোল্ডেন ব্রাঞ্চ ডিজাইনের ফ্রি-সাইজ এডজাস্টেবল আংটি।",
                    purchaseCost = 450.0,
                    sellingPrice = 850.0,
                    stock = 50,
                    lowStockThreshold = 10,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-RNG-002",
                    name = "Solitaire Floral Crystal Ring (সলিটেয়ার ক্রিস্টাল আংটি)",
                    category = "Finger Ring",
                    description = "ক্লাসিক সলিটেয়ার ডিজাইনের স্পার্কলিং ক্রিস্টাল আংটি। ভালোবাসার শ্রেষ্ঠ উপহার।",
                    purchaseCost = 380.0,
                    sellingPrice = 750.0,
                    stock = 45,
                    lowStockThreshold = 8,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                // 5. Earring (কানের দুল)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-EAR-001",
                    name = "Floral Sui-Suta Gold Threader Earrings (ফ্লোরাল সুই-সুতা কানের দুল)",
                    category = "Earring",
                    description = "সোনালী ফ্লোরাল নকশা ও ঝুলন্ত চেইন সুই-সুতা প্রিমিয়াম পার্টি দুল (PH-54-195)।",
                    purchaseCost = 380.0,
                    sellingPrice = 690.0,
                    stock = 60,
                    lowStockThreshold = 10,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-EAR-002",
                    name = "Traditional Meenakari Jhumka (ঐতিহ্যবাহী মীনাকারি ঝুমকা)",
                    category = "Earring",
                    description = "হাতে তৈরি রঙিন মীনাকারি নকশা ও পার্ল ড্রপস সমৃদ্ধ জমকালো ঝুমকা।",
                    purchaseCost = 480.0,
                    sellingPrice = 890.0,
                    stock = 40,
                    lowStockThreshold = 8,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                // 6. Payel (পায়েল)
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-PYL-001",
                    name = "Sleek Snake Chain Heart Payel (স্নেক চেইন হার্ট চার্ম পায়েল)",
                    category = "Payel",
                    description = "মসৃণ ১৮ ক্যারেট গোল্ড প্লেটিং স্নেক চেইন এবং কিউট ঝুলন্ত হার্ট চার্ম সম্বলিত মার্জিত পায়েল।",
                    purchaseCost = 450.0,
                    sellingPrice = 850.0,
                    stock = 35,
                    lowStockThreshold = 6,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                ),
                Product(
                    id = UUID.randomUUID().toString(),
                    sku = "MNR-PYL-002",
                    name = "Bridal Silver Ghungroo Payel (ব্রাইডাল সিলভার ঘুঙুর পায়েল)",
                    category = "Payel",
                    description = "মিষ্টি টুংটাং শব্দের খাঁটি সিলভার লুক ব্রাইডাল ঘুঙুর পায়েল সেট।",
                    purchaseCost = 580.0,
                    sellingPrice = 1050.0,
                    stock = 30,
                    lowStockThreshold = 5,
                    status = ProductStatus.ACTIVE,
                    createdBy = user.id,
                    createdByName = user.displayName
                )
            )
            for (item in items) {
                stockRepo.addProduct(item)
            }
            showMessage("মনের উপহার জুয়েলারি কালেকশন সফলভাবে লোড হয়েছে! (12 Items)")
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

    /**
     * Reverses sale, restores inventory stock atomically, and records audit trail
     */
    fun cancelOrRefundSale(transactionId: String, reason: String = "Customer return / Refund") {
        val user = currentUser.value ?: return
        if (user.role != UserRole.ADMIN) {
            showMessage("Only Admin can cancel transactions and restore stock.")
            return
        }
        viewModelScope.launch {
            val res = stockRepo.cancelOrRefundSale(transactionId, user, reason)
            res.onSuccess {
                showMessage("Sale cancelled. Items stock successfully restored to inventory.")
            }.onFailure {
                showMessage("Refund failed: ${it.message}")
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

    fun importBackupJson(jsonString: String) {
        val user = currentUser.value ?: return
        if (user.role != UserRole.ADMIN) {
            showMessage("Only Administrator can import database backups.")
            return
        }
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(jsonString)
                var count = 0
                if (root.has("products")) {
                    val array = root.getJSONArray("products")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val sku = obj.optString("sku", "SKU-$i").trim().uppercase()
                        val name = obj.optString("name", "Product $i").trim()
                        val stock = obj.optInt("stock", 0)
                        val price = obj.optDouble("price", 0.0)
                        val cost = obj.optDouble("cost", if (price > 0) price * 0.7 else 0.0)
                        val cat = obj.optString("category", "General").trim()

                        val existing = activeProducts.value.find { it.sku.equals(sku, ignoreCase = true) }
                        if (existing != null) {
                            stockRepo.updateProduct(existing.copy(
                                name = name,
                                stock = stock,
                                sellingPrice = price,
                                purchaseCost = cost,
                                category = cat,
                                status = if (stock == 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE
                            ))
                        } else {
                            val newP = Product(
                                id = UUID.randomUUID().toString(),
                                sku = sku,
                                name = name,
                                category = cat,
                                description = "Imported from backup",
                                purchaseCost = cost,
                                sellingPrice = price,
                                stock = stock,
                                lowStockThreshold = 5,
                                status = if (stock == 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE,
                                createdBy = user.id,
                                createdByName = user.displayName
                            )
                            stockRepo.addProduct(newP)
                        }
                        count++
                    }
                }
                showMessage("Successfully imported $count products from backup!")
            } catch (e: Exception) {
                showMessage("Import failed: ${e.localizedMessage ?: "Invalid JSON format"}")
            }
        }
    }

    fun logout() {
        authRepo.logout()
        clearCart()
        _currentDestination.value = NavigationDestination.Dashboard
    }
}

package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import java.util.Locale

@Composable
fun ProductsScreen(viewModel: MainViewModel) {
    val products by viewModel.activeProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isAddEditOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = products.filter { prod ->
        val matchesQuery = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.sku.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || prod.category == selectedCategory
        matchesQuery && matchesCategory
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    isAddEditOpen = true
                },
                containerColor = IndigoPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with CSV Export
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Product Inventory",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${products.size} items registered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        val csv = viewModel.stockRepo.exportCsv(products)
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Export Inventory CSV")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontSize = 12.sp)
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search by product name, SKU, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(IndigoPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            text = if (products.isEmpty()) "Store Inventory is Empty (0 Items)" else "No matching products found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (products.isEmpty()) "Add products with custom selling price and stock to start organizing your store." else "Try searching with a different SKU or name.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (products.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    editingProduct = null
                                    isAddEditOpen = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Product", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            canDelete = currentUser?.role == UserRole.ADMIN,
                            onEdit = {
                                editingProduct = product
                                isAddEditOpen = true
                            },
                            onDelete = {
                                productToDelete = product
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Dialog
    if (isAddEditOpen) {
        AddEditProductDialog(
            product = editingProduct,
            onDismiss = { isAddEditOpen = false },
            onSave = { sku, name, cat, desc, cost, price, stock, threshold ->
                viewModel.saveProduct(
                    id = editingProduct?.id,
                    sku = sku,
                    name = name,
                    category = cat,
                    description = desc,
                    cost = cost,
                    price = price,
                    stock = stock,
                    threshold = threshold,
                    onDone = { isAddEditOpen = false }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Delete Product", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${prod.name}' (SKU: ${prod.sku})? This action can only be performed by Administrators.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SKU: ${product.sku} • ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock Badge
                val isOut = product.stock == 0
                val isLow = product.stock in 1..product.lowStockThreshold
                val badgeColor = when {
                    isOut -> RoseDanger
                    isLow -> AmberWarning
                    else -> EmeraldSecondary
                }
                val badgeText = when {
                    isOut -> "Out of Stock"
                    isLow -> "${product.stock} Low Stock"
                    else -> "${product.stock} In Stock"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Selling Price: ৳ ${String.format(Locale.US, "%,.2f", product.sellingPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                    Text(
                        text = "Cost: ৳ ${String.format(Locale.US, "%,.2f", product.purchaseCost)} • Min alert: ${product.lowStockThreshold}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = IndigoPrimary)
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = RoseDanger)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (sku: String, name: String, cat: String, desc: String, cost: Double, price: Double, stock: Int, threshold: Int) -> Unit
) {
    var sku by remember { mutableStateOf(product?.sku ?: "PRN-${(10000..99999).random()}") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Hardware & POS") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var costStr by remember { mutableStateOf(product?.purchaseCost?.toString() ?: "") }
    var priceStr by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "10") }
    var thresholdStr by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Add New Product" else "Edit Product",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (error != null) {
                    Text(text = error!!, color = RoseDanger, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Product SKU Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = product == null // SKU locked on edit
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Cost (৳)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (৳)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = thresholdStr,
                        onValueChange = { thresholdStr = it },
                        label = { Text("Min Alert Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || sku.isBlank()) {
                        error = "SKU and Name are required."
                        return@Button
                    }
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toIntOrNull() ?: 0
                    val thresh = thresholdStr.toIntOrNull() ?: 5
                    if (cost < 0 || price < 0 || stock < 0) {
                        error = "Numbers cannot be negative."
                        return@Button
                    }
                    onSave(sku, name, category, desc, cost, price, stock, thresh)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.ProductAssetResolver
import com.example.ui.components.bounceClick
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.MonerGold
import com.example.ui.theme.MonerGoldLight
import com.example.ui.theme.MonerRuby
import com.example.ui.theme.MonerRubyDark
import com.example.ui.theme.RoseDanger
import java.util.Locale

/**
 * The 6 authentic Moner Upohar jewelry categories with their genuine photo drawables.
 */
data class JewelryCategoryItem(
    val key: String,
    val titleEn: String,
    val titleBn: String,
    val drawableRes: Int
)

val MONER_UPOHAR_CATEGORIES = listOf(
    JewelryCategoryItem("All", "All Items", "সকল কালেকশন", R.drawable.moner_upohar_logo),
    JewelryCategoryItem("Necklace", "Necklace", "নেকলেস / গলার হার", R.drawable.cat_necklace),
    JewelryCategoryItem("Bracelet", "Bracelet", "ব্রেসলেট", R.drawable.cat_bracelet),
    JewelryCategoryItem("Kashmiri Churi", "Kashmiri Churi", "কাশ্মীরি চুড়ি", R.drawable.cat_kashmiri_churi),
    JewelryCategoryItem("Finger Ring", "Finger Ring", "আংটি / রিং", R.drawable.cat_finger_ring),
    JewelryCategoryItem("Earring", "Earring", "কানের দুল", R.drawable.cat_earring),
    JewelryCategoryItem("Payel", "Payel", "পায়েল / নূপুর", R.drawable.cat_payel)
)

private fun matchesJewelryCategory(productCat: String, targetKey: String): Boolean {
    if (targetKey == "All") return true
    val p = productCat.trim().lowercase()
    val t = targetKey.trim().lowercase()
    return p == t || p.contains(t) || t.contains(p) ||
            (t.contains("necklace") && (p.contains("necklace") || p.contains("হার"))) ||
            (t.contains("bracelet") && (p.contains("bracelet") || p.contains("ব্রেসলেট"))) ||
            (t.contains("churi") && (p.contains("churi") || p.contains("চুড়ি") || p.contains("চুড়ি"))) ||
            (t.contains("ring") && (p.contains("ring") || p.contains("আংটি"))) ||
            (t.contains("earring") && (p.contains("earring") || p.contains("দুল"))) ||
            (t.contains("payel") && (p.contains("payel") || p.contains("পায়েল") || p.contains("নূপুর")))
}

@Composable
fun ProductsScreen(viewModel: MainViewModel) {
    val products by viewModel.activeProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val globalCategory by viewModel.selectedCategoryFilter.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStockFilter by remember { mutableStateOf("All Stock") }
    var isAddEditOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var productForQuickRestock by remember { mutableStateOf<Product?>(null) }

    // Sync when category is navigated from Dashboard or other screens
    LaunchedEffect(globalCategory) {
        if (globalCategory.isNotBlank()) {
            selectedCategory = globalCategory
        }
    }

    val stockFilters = listOf("All Stock", "In Stock", "Low Stock", "Out of Stock")

    val filteredProducts = products.filter { prod ->
        val matchesQuery = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.sku.contains(searchQuery, ignoreCase = true) ||
                prod.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = matchesJewelryCategory(prod.category, selectedCategory)
        val matchesStock = when (selectedStockFilter) {
            "In Stock" -> prod.stock > prod.lowStockThreshold
            "Low Stock" -> prod.stock in 1..prod.lowStockThreshold
            "Out of Stock" -> prod.stock == 0
            else -> true
        }
        matchesQuery && matchesCategory && matchesStock
    }

    Scaffold(
        floatingActionButton = {
            // RBAC: Only Admin can add products. Staff can only sell and view stock.
            if (currentUser?.role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        isAddEditOpen = true
                    },
                    containerColor = MonerRuby,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                }
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
                        text = "মনের উপহার — জুয়েলারি ইনভেন্টরি",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${products.size} items • ${products.sumOf { it.stock }} units in boutique stock",
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
                placeholder = { Text("Search jewelry by name, SKU, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // 6 AUTHENTIC JEWELRY CATEGORY CAROUSEL (USER'S CORE REQUEST)
            // ==========================================
            Text(
                text = "৬টি প্রিমিয়াম জুয়েলারি ক্যাটাগরি (Select Category to View & Add Stock):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(MONER_UPOHAR_CATEGORIES) { catItem ->
                    val isSelected = selectedCategory == catItem.key
                    val catProducts = if (catItem.key == "All") products else products.filter {
                        matchesJewelryCategory(it.category, catItem.key)
                    }
                    val catStock = catProducts.sumOf { it.stock }

                    CategoryShelfCard(
                        category = catItem,
                        productCount = catProducts.size,
                        stockTotal = catStock,
                        isSelected = isSelected,
                        onClick = {
                            selectedCategory = catItem.key
                            viewModel.selectCategory(catItem.key)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stockFilters.forEach { filter ->
                    FilterChip(
                        selected = selectedStockFilter == filter,
                        onClick = { selectedStockFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (filter == "Out of Stock") RoseDanger else MonerGold,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Category Status Bar with Quick "+ Add Product" shortcut
            if (selectedCategory != "All") {
                val catObj = MONER_UPOHAR_CATEGORIES.find { it.key == selectedCategory }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonerRuby.copy(alpha = 0.08f))
                        .border(1.dp, MonerRuby.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${catObj?.titleEn ?: selectedCategory} (${catObj?.titleBn ?: ""})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MonerRuby
                        )
                        Text(
                            text = "${filteredProducts.size} items • ${filteredProducts.sumOf { it.stock }} units available",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentUser?.role == UserRole.ADMIN) {
                        Button(
                            onClick = {
                                editingProduct = null
                                isAddEditOpen = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MonerRuby),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to ${catObj?.titleEn ?: "Category"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
                        val activeImg = MONER_UPOHAR_CATEGORIES.find { it.key == selectedCategory }?.drawableRes
                            ?: R.drawable.moner_upohar_logo

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MonerRuby.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = activeImg),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = if (products.isEmpty()) "Store Inventory is Empty (0 Items)" else "No matching jewelry items found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (products.isEmpty()) "Add products with custom selling price and stock to start organizing your boutique." else "Try switching the category or resetting your search filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (products.isEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        editingProduct = null
                                        isAddEditOpen = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MonerRuby)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Custom Item", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.seedMonerUpoharCatalog() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MonerGold),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MonerGold)
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MonerGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load 6 Categories Stock", fontWeight = FontWeight.Bold)
                                }
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
                            isAdmin = currentUser?.role == UserRole.ADMIN,
                            onEdit = {
                                editingProduct = product
                                isAddEditOpen = true
                            },
                            onDelete = {
                                productToDelete = product
                            },
                            onRestock = {
                                productForQuickRestock = product
                            }
                        )
                    }
                }
            }
        }
    }

    // Quick Restock Dialog ("tar vitore product image soho stoke ad kora jabe")
    if (productForQuickRestock != null) {
        val prod = productForQuickRestock!!
        QuickRestockDialog(
            product = prod,
            onDismiss = { productForQuickRestock = null },
            onConfirmRestock = { delta, note ->
                viewModel.quickAdjustStock(prod.id, delta, note)
                productForQuickRestock = null
            }
        )
    }

    // Add / Edit Modal Dialog with Category Photo Preview
    if (isAddEditOpen) {
        AddEditProductDialog(
            product = editingProduct,
            initialCategory = if (selectedCategory != "All") selectedCategory else "Necklace",
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

/**
 * Visual Category Shelf Card showing authentic jewelry photo and live stock
 */
@Composable
fun CategoryShelfCard(
    category: JewelryCategoryItem,
    productCount: Int,
    stockTotal: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .bounceClick(scaleDown = 0.95f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MonerRuby.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MonerGold) else CardDefaults.outlinedCardBorder()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
            ) {
                Image(
                    painter = painterResource(id = category.drawableRes),
                    contentDescription = category.titleEn,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Selection checkmark or count badge overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MonerRuby)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = category.titleEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MonerRuby else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = category.titleBn,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$productCount items",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$stockTotal stock",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stockTotal > 0) MonerGold else RoseDanger
                    )
                }
            }
        }
    }
}

/**
 * Product Card with high-res photo, pricing, stock badge, and fast "+ Stock" restock button
 */
@Composable
fun ProductItemCard(
    product: Product,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRestock: () -> Unit
) {
    val imgRes = ProductAssetResolver.getDrawableForProduct(product)
    val isOut = product.stock == 0
    val isLow = product.stock in 1..product.lowStockThreshold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.97f),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOut) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isOut) androidx.compose.foundation.BorderStroke(1.dp, RoseDanger.copy(alpha = 0.5f)) else CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Thumbnail Image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .border(1.dp, MonerGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "SKU: ${product.sku} • ${product.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stock Badge (clickable to trigger restock)
                val badgeColor = when {
                    isOut -> RoseDanger
                    isLow -> AmberWarning
                    else -> MonerGold
                }
                val badgeText = when {
                    isOut -> "OUT OF STOCK"
                    isLow -> "${product.stock} Low Stock"
                    else -> "${product.stock} In Stock"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .clickable(onClick = onRestock)
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

            // Visible Out-of-Stock Alert Banner
            if (isOut) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoseDanger.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "স্টক শেষ (Out of Stock) - রি-স্টক না করা পর্যন্ত বিক্রয় বন্ধ",
                            color = RoseDanger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing details & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.2f", product.sellingPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MonerRuby
                    )
                    Text(
                        text = "Cost: ৳ ${String.format(Locale.US, "%,.2f", product.purchaseCost)} • Min alert: ${product.lowStockThreshold}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quick Restock Button directly on product item card
                    Button(
                        onClick = onRestock,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonerRuby.copy(alpha = 0.12f),
                            contentColor = MonerRuby
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MonerRuby)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MonerRuby)
                    }

                    // Admin Edit / Delete Actions
                    if (isAdmin) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = MonerRuby)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = RoseDanger)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Quick Restock Modal Dialog
 * Allows adding stock with live preview of the jewelry item.
 */
@Composable
fun QuickRestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirmRestock: (delta: Int, note: String) -> Unit
) {
    var addAmountStr by remember { mutableStateOf("10") }
    var note by remember { mutableStateOf("Restock in category") }
    val imgRes = ProductAssetResolver.getDrawableForProduct(product)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Stock to Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Header with Image Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = imgRes),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Category: ${product.category}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Current Stock: ${product.stock} units",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MonerGold
                        )
                    }
                }

                Text(
                    text = "Quick Select Quantity:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                // Quick Increment Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 20, 50).forEach { amount ->
                        OutlinedButton(
                            onClick = { addAmountStr = amount.toString() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (addAmountStr == amount.toString()) MonerRuby else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = "+$amount",
                                fontWeight = FontWeight.Bold,
                                color = if (addAmountStr == amount.toString()) MonerRuby else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = addAmountStr,
                    onValueChange = { addAmountStr = it },
                    label = { Text("Stock Quantity to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Audit Note / Supplier Ref") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val delta = addAmountStr.toIntOrNull() ?: 0
                val newProjected = (product.stock + delta).coerceAtLeast(0)
                Text(
                    text = "New projected stock: $newProjected units (+ $delta)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MonerRuby
                )
            }
        },
        confirmButton = {
            val delta = addAmountStr.toIntOrNull() ?: 0
            Button(
                onClick = {
                    if (delta > 0) {
                        onConfirmRestock(delta, note.ifBlank { "Restock in category" })
                    }
                },
                enabled = delta > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MonerRuby)
            ) {
                Text("Confirm Restock", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Add / Edit Product Dialog with live category photo preview and 6 visual category selectors
 */
@Composable
fun AddEditProductDialog(
    product: Product?,
    initialCategory: String = "Necklace",
    onDismiss: () -> Unit,
    onSave: (sku: String, name: String, cat: String, desc: String, cost: Double, price: Double, stock: Int, threshold: Int) -> Unit
) {
    var sku by remember { mutableStateOf(product?.sku ?: "MNR-${(10000..99999).random()}") }
    var name by remember { mutableStateOf(product?.name ?: "") }
    var category by remember { mutableStateOf(product?.category ?: initialCategory) }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var costStr by remember { mutableStateOf(product?.purchaseCost?.toString() ?: "") }
    var priceStr by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "15") }
    var thresholdStr by remember { mutableStateOf(product?.lowStockThreshold?.toString() ?: "5") }
    var error by remember { mutableStateOf<String?>(null) }

    val categoryPhotoRes = ProductAssetResolver.getDrawableForCategory(category)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Add Jewelry Product" else "Edit Jewelry Product",
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

                // Dynamic Category Photo Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .border(1.dp, MonerGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(id = categoryPhotoRes),
                        contentDescription = category,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Category Photo: $category",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Select From 6 Jewelry Categories:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 6 Category Quick Selectors with mini previews
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "Necklace" to R.drawable.cat_necklace,
                        "Bracelet" to R.drawable.cat_bracelet,
                        "Kashmiri Churi" to R.drawable.cat_kashmiri_churi,
                        "Finger Ring" to R.drawable.cat_finger_ring,
                        "Earring" to R.drawable.cat_earring,
                        "Payel" to R.drawable.cat_payel
                    )
                    presets.forEach { (catName, catImg) ->
                        val isSelected = category.equals(catName, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MonerRuby else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .border(
                                    1.dp,
                                    if (isSelected) MonerGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { category = catName }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = catImg),
                                contentDescription = catName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = catName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Product SKU Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = product == null
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (e.g. Royal Gold Necklace)") },
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
                        label = { Text("Initial Stock") },
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
                    label = { Text("Jewelry Description & Specifications") },
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
                    if (price <= 0.0) {
                        error = "Selling price must be greater than ৳ 0."
                        return@Button
                    }
                    if (cost < 0 || stock < 0 || thresh < 0) {
                        error = "Cost, stock, and alert threshold cannot be negative."
                        return@Button
                    }
                    onSave(sku, name, category, desc, cost, price, stock, thresh)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MonerRuby)
            ) {
                Text("Save Product", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

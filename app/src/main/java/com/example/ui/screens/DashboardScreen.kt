package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryChangeType
import com.example.ui.MainViewModel
import com.example.ui.NavigationDestination
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.BanglalinkPromoHeroBanner
import com.example.ui.components.ProductAssetResolver
import com.example.ui.components.bounceClick
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.BanglalinkOrangeDark
import com.example.ui.theme.BanglalinkOrangeLight
import com.example.ui.theme.PrantejDeepEmerald
import com.example.ui.theme.PrantejEmeraldBright
import com.example.ui.theme.PrantejGold
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val products by viewModel.activeProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    val lowStockProducts = products.filter { it.stock <= it.lowStockThreshold }
    val recentTxs = transactions.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val settings by viewModel.allSettings.collectAsState()
            val shareUrl = settings.find { it.key == "public_share_url" }?.value
                ?: "https://ais-pre-a3omklob3sh7jfdsfqrb6o-760393657966.asia-northeast1.run.app"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PRANTEJ Store Dashboard",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clean inventory overview and daily sales counters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "PRANTEJ CloudStock App Download & Web Link:\n$shareUrl")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Share App Link"))
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Link", fontSize = 12.sp)
                }
            }
        }

        // Promotional Hero Banner with Banglalink Vibrant Styling
        item {
            BanglalinkPromoHeroBanner(
                onActionClick = { viewModel.navigateTo(NavigationDestination.POS) }
            )
        }

        // Welcome Setup Card when store has 0 products
        if (products.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BanglalinkOrange.copy(alpha = 0.07f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BanglalinkOrange.copy(alpha = 0.35f)))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BanglalinkOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = BanglalinkOrange, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ready to start fresh!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "All counters are initialized at 0. Add your items to begin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "This private store app is set up completely clean. You can add your products, set your custom prices, and organize your stock according to your business needs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.navigateTo(NavigationDestination.Products) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BanglalinkOrange)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Product", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Featured Products & Product Visuals Section (Requirement 3)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(BanglalinkOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = BanglalinkOrange, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Featured Product Gallery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = if (products.isEmpty()) "Quick Add" else "View Catalog",
                            style = MaterialTheme.typography.labelMedium,
                            color = BanglalinkOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                viewModel.navigateTo(NavigationDestination.Products)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (products.isEmpty()) {
                        // Interactive Preview Showcase when store starts from 0
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            val sampleGallery = listOf(
                                Triple("Flagship Smartphone", "Electronics", R.drawable.prod_flagship_phone_1792690428158),
                                Triple("Smart Watch AMOLED", "Wearables", R.drawable.prod_smart_watch_1792690441179),
                                Triple("Wireless Earbuds ANC", "Audio", R.drawable.prod_earbuds_case_1792690455207),
                                Triple("65W GaN Fast Charger", "Accessories", R.drawable.prod_fast_charger_1792690476604)
                            )
                            items(sampleGallery) { item ->
                                Card(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clickable { viewModel.navigateTo(NavigationDestination.Products) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column {
                                        Image(
                                            painter = painterResource(id = item.third),
                                            contentDescription = item.first,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                        )
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = item.first,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = item.second,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BanglalinkOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "+ Add to Stock",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrantejEmeraldBright,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Live Store Products with Real Visuals & 1-Tap POS Checkout
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(products.take(8), key = { it.id }) { product ->
                                val imageRes = ProductAssetResolver.getDrawableForProduct(product)
                                Card(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clickable {
                                            viewModel.addToCart(product)
                                            viewModel.navigateTo(NavigationDestination.POS)
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column {
                                        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                                            Image(
                                                painter = painterResource(id = imageRes),
                                                contentDescription = product.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black.copy(alpha = 0.7f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    .align(Alignment.TopEnd)
                                            ) {
                                                Text(
                                                    text = "${product.stock} in stock",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (product.stock <= product.lowStockThreshold) AmberWarning else PrantejEmeraldBright
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = product.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = product.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "৳ ${String.format(Locale.US, "%,.0f", product.sellingPrice)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = BanglalinkOrange
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(BanglalinkOrange)
                                                        .clickable {
                                                            viewModel.addToCart(product)
                                                            viewModel.navigateTo(NavigationDestination.POS)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.AddShoppingCart,
                                                        contentDescription = "Sell",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4 Key KPI Cards (Starts strictly at 0)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Today's Sales",
                        value = "৳ ${String.format(Locale.US, "%,.0f", metrics.todaySalesTotal)}",
                        subtitle = "${metrics.todayOrdersCount} orders today",
                        icon = Icons.Default.AttachMoney,
                        accentColor = BanglalinkOrange,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Inventory Value",
                        value = "৳ ${String.format(Locale.US, "%,.0f", metrics.totalInventoryValue)}",
                        subtitle = "${metrics.totalStockUnits} units in stock",
                        icon = Icons.Default.Inventory2,
                        accentColor = PrantejEmeraldBright,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Active Products",
                        value = "${metrics.totalActiveProducts}",
                        subtitle = "Catalog items",
                        icon = Icons.Default.Inventory,
                        accentColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Stock Warnings",
                        value = "${metrics.lowStockCount + metrics.outOfStockCount}",
                        subtitle = "${metrics.outOfStockCount} out of stock",
                        icon = Icons.Default.Warning,
                        accentColor = if (metrics.outOfStockCount > 0) RoseDanger else AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Action Shortcuts with Banglalink Vibrant Styling
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Store Operations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(NavigationDestination.POS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BanglalinkOrange)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Sale", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(NavigationDestination.Products) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrantejDeepEmerald)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrantejGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Products", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrantejGold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(NavigationDestination.Logs) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit Logs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }


        // Low Stock Urgency Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Restock Urgency (${lowStockProducts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (lowStockProducts.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All stock levels are clear and healthy.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmeraldSecondary
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lowStockProducts.take(4).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "SKU: ${prod.sku} • Min alert: ${prod.lowStockThreshold}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (prod.stock == 0) RoseDanger.copy(alpha = 0.15f)
                                                    else AmberWarning.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (prod.stock == 0) "Out of Stock" else "${prod.stock} left",
                                                color = if (prod.stock == 0) RoseDanger else AmberWarning,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                viewModel.adjustStock(
                                                    productId = prod.id,
                                                    newStock = prod.stock + 10,
                                                    changeType = InventoryChangeType.RESTOCK,
                                                    reason = "Quick +10 restock",
                                                    onDone = {}
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("+10", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (recentTxs.isNotEmpty()) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = IndigoLight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    viewModel.navigateTo(NavigationDestination.Transactions)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentTxs.isEmpty()) {
                        Text(
                            text = "No sales recorded yet. Completed sales from the POS register will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentTxs.forEach { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = tx.invoiceNumber,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IndigoLight
                                        )
                                        Text(
                                            text = "${tx.cashierName} • ${SimpleDateFormat("hh:mm a", Locale.US).format(Date(tx.timestamp))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "৳ ${String.format(Locale.US, "%,.2f", tx.totalAmount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSecondary
                                        )
                                        Text(
                                            text = tx.paymentMethod.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

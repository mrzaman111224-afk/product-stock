package com.example.ui.screens

import android.content.Intent
import com.example.ui.NavigationDestination
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CartItem
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.TransactionRecord
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun POSScreen(viewModel: MainViewModel) {
    val products by viewModel.activeProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val discount by viewModel.discount.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val isProcessing by viewModel.isProcessingSale.collectAsState()
    val completedTx by viewModel.completedTransaction.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var discountInput by remember { mutableStateOf("") }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val availableProducts = products.filter {
        val matchesQuery = it.name.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
        matchesQuery && matchesCategory
    }

    val subtotal = cart.sumOf { it.subtotal }
    val payableTotal = (subtotal - discount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Top Search & Category Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search catalog to add...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Catalog Grid (Top/Middle section)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            if (availableProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (products.isEmpty()) "Catalog is empty (0 products)" else "No matching items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (products.isEmpty()) "Add products to inventory first to start billing sales." else "Clear search to see available products.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (products.isEmpty()) {
                            Button(
                                onClick = { viewModel.navigateTo(NavigationDestination.Products) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Add Products Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableProducts, key = { it.id }) { product ->
                        val isOut = product.stock <= 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isOut) { viewModel.addToCart(product) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOut) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    color = if (isOut) Slate400 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = product.sku,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "৳ ${String.format(Locale.US, "%,.0f", product.sellingPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOut) Slate400 else EmeraldSecondary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isOut) RoseDanger.copy(alpha = 0.15f)
                                                else IndigoPrimary.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isOut) "Empty" else "${product.stock}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOut) RoseDanger else IndigoPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Cart Panel (Bottom section)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Cart Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Current Sale (${cart.sumOf { it.quantity }} items)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (cart.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoseDanger,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.clearCart() }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Cart item list
                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Slate400, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Cart is empty. Tap any catalog item to add.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(cart, key = { it.productId }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "৳ ${item.unitPrice} each • ৳ ${String.format(Locale.US, "%,.2f", item.subtotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(item.productId, -1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(item.productId, 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeFromCart(item.productId) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = RoseDanger, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Billing Controls & Checkout
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Payment Method Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { viewModel.setPaymentMethod(method) },
                                label = {
                                    Text(
                                        when (method) {
                                            PaymentMethod.CASH -> "Cash"
                                            PaymentMethod.CARD -> "Card"
                                            PaymentMethod.MFS_BKASH -> "bKash"
                                            PaymentMethod.MFS_NAGAD -> "Nagad"
                                            PaymentMethod.BANK_TRANSFER -> "Bank"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        when (method) {
                                            PaymentMethod.CASH -> Icons.Default.LocalAtm
                                            PaymentMethod.CARD -> Icons.Default.CreditCard
                                            PaymentMethod.MFS_BKASH, PaymentMethod.MFS_NAGAD -> Icons.Default.PhoneAndroid
                                            PaymentMethod.BANK_TRANSFER -> Icons.Default.LocalAtm
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IndigoPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Discount input
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Discount (৳): ", style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = {
                                    discountInput = it
                                    val d = it.toDoubleOrNull() ?: 0.0
                                    viewModel.setDiscount(d)
                                },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Payable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "৳ ${String.format(Locale.US, "%,.2f", payableTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.checkoutSale() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        enabled = cart.isNotEmpty() && !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Complete Sale (৳ ${String.format(Locale.US, "%,.2f", payableTotal)})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Digital Printable Receipt Modal
    if (completedTx != null) {
        ReceiptDialog(
            tx = completedTx!!,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
fun ReceiptDialog(
    tx: TransactionRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldSecondary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PRANTEJ CLOUDSTOCK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Commercial Store Invoice",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Invoice metadata box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Invoice #:", style = MaterialTheme.typography.labelSmall, color = Slate700)
                            Text(tx.invoiceNumber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date:", style = MaterialTheme.typography.labelSmall, color = Slate700)
                            Text(
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(tx.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate900
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cashier:", style = MaterialTheme.typography.labelSmall, color = Slate700)
                            Text(tx.cashierName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Method:", style = MaterialTheme.typography.labelSmall, color = Slate700)
                            Text(tx.paymentMethod.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Items list
                Text(
                    text = "Purchased Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = tx.itemsJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate800,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Totals
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", color = Slate700)
                        Text("৳ ${String.format(Locale.US, "%,.2f", tx.subtotal)}", color = Slate900, fontWeight = FontWeight.SemiBold)
                    }
                    if (tx.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", color = RoseDanger)
                            Text("-৳ ${String.format(Locale.US, "%,.2f", tx.discount)}", color = RoseDanger, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total Paid:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                        Text(
                            "৳ ${String.format(Locale.US, "%,.2f", tx.totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barcode simulation
                Text(
                    text = "||||| ||||||| |||| |||||| |||||||| |||||",
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = Slate400,
                    fontSize = 16.sp
                )
                Text(
                    text = "Thank you for shopping with PRANTEJ Store!",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val receiptText = """
                                PRANTEJ CLOUDSTOCK INVOICE
                                ---------------------------
                                Invoice: ${tx.invoiceNumber}
                                Date: ${Date(tx.timestamp)}
                                Cashier: ${tx.cashierName}
                                Method: ${tx.paymentMethod.name}
                                
                                Items:
                                ${tx.itemsJson}
                                
                                Subtotal: ৳${tx.subtotal}
                                Discount: ৳${tx.discount}
                                Total Paid: ৳${tx.totalAmount}
                                ---------------------------
                                Thank you for shopping!
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("New Sale", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

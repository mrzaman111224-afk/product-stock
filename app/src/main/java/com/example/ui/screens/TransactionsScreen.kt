package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionRecord
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val label: String, val days: Int) {
    TODAY("Today (আজ)", 1),
    WEEK("7 Days (সাপ্তাহিক)", 7),
    MONTH("30 Days (মাসিক)", 30),
    ALL("All Time (সব)", 9999)
}

@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    var search by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.ALL) }
    var inspectingTx by remember { mutableStateOf<TransactionRecord?>(null) }
    var txToCancel by remember { mutableStateOf<TransactionRecord?>(null) }

    // Period boundary timestamp
    val periodStartTimestamp = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            ReportPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            ReportPeriod.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            ReportPeriod.MONTH -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.timeInMillis
            }
            ReportPeriod.ALL -> 0L
        }
    }

    val periodTransactions = remember(transactions, periodStartTimestamp) {
        if (periodStartTimestamp == 0L) transactions
        else transactions.filter { it.timestamp >= periodStartTimestamp }
    }

    val totalSales = periodTransactions.sumOf { it.totalAmount }
    val estimatedProfit = totalSales * 0.35 // 35% average gross margin for health/store items
    val totalOrders = periodTransactions.size
    val totalUnitsSold = periodTransactions.sumOf { it.itemCount }

    val filtered = periodTransactions.filter {
        it.invoiceNumber.contains(search, ignoreCase = true) ||
                it.cashierName.contains(search, ignoreCase = true) ||
                it.paymentMethod.name.contains(search, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header with CSV Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sales & Reports (বিক্রয় ও রিপোর্ট)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Daily, weekly, monthly profit & transaction ledger",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = {
                    val csv = viewModel.stockRepo.exportSalesCsv(periodTransactions)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csv)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export Sales CSV / Excel"))
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Period Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportPeriod.values().forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { selectedPeriod = period },
                    label = { Text(period.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BanglalinkOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sales & Profit Reporting Metric Cards (Feature 3)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = BanglalinkOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sales Revenue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.0f", totalSales)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BanglalinkOrange
                    )
                    Text(
                        text = "$totalOrders orders • $totalUnitsSold items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = EmeraldSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Est. Gross Profit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ ${String.format(Locale.US, "%,.0f", estimatedProfit)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSecondary
                    )
                    Text(
                        text = "~35% margin (আনুমানিক লাভ)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by invoice #, cashier, or method...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Transaction List
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found for ${selectedPeriod.label}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { inspectingTx = tx },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.invoiceNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                                Text(
                                    text = "Cashier: ${tx.cashierName} • ${tx.itemCount} item(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(tx.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IndigoPrimary.copy(alpha = 0.1f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tx.paymentMethod.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detailed Receipt & Refund Modal
    if (inspectingTx != null) {
        val tx = inspectingTx!!
        val cleanItems = remember(tx.itemsJson) {
            if (tx.itemsJson.startsWith("RAW:") && tx.itemsJson.contains("###")) {
                tx.itemsJson.substringAfter("###")
            } else {
                tx.itemsJson
            }
        }

        Dialog(onDismissRequest = { inspectingTx = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BanglalinkOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = BanglalinkOrange, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("মনের উপহার (Moner Upohar)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("— প্রিয়জনের জন্য, হৃদয়ের উপহার — জুয়েলারি মেমো", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Invoice:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tx.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Date:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(tx.timestamp)), style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cashier:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tx.cashierName, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(tx.paymentMethod.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Text("Purchased Items:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = cleanItems,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text("৳ ${String.format(Locale.US, "%,.2f", tx.subtotal)}", fontWeight = FontWeight.SemiBold)
                    }
                    if (tx.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", color = RoseDanger, style = MaterialTheme.typography.bodyMedium)
                            Text("-৳ ${String.format(Locale.US, "%,.2f", tx.discount)}", color = RoseDanger, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("৳ ${String.format(Locale.US, "%,.2f", tx.totalAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BanglalinkOrange)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons (WhatsApp Share + Admin Refund + Close)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val receiptText = """
                                    🎁 *মনের উপহার (MONER UPOHAR) INVOICE* 🎁
                                    — প্রিয়জনের জন্য, হৃদয়ের উপহার —
                                    -----------------------------
                                    Invoice: ${tx.invoiceNumber}
                                    Date: ${SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US).format(Date(tx.timestamp))}
                                    Cashier: ${tx.cashierName}
                                    Payment: ${tx.paymentMethod.name}
                                    
                                    *Items:*
                                    $cleanItems
                                    
                                    Subtotal: ৳${tx.subtotal}
                                    Discount: ৳${tx.discount}
                                    *Total Paid: ৳${tx.totalAmount}*
                                    -----------------------------
                                    Thank you for shopping with মনের উপহার!
                                """.trimIndent()

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, receiptText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Receipt via WhatsApp"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share via WhatsApp / Print", fontWeight = FontWeight.Bold)
                        }

                        // Admin Refund / Cancel Transaction Button (BUG 3: restores stock atomically)
                        if (currentUser?.role == UserRole.ADMIN) {
                            OutlinedButton(
                                onClick = { txToCancel = tx },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseDanger),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoseDanger),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel & Refund Sale (Restore Stock)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = { inspectingTx = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Cancel / Refund Confirmation Dialog
    if (txToCancel != null) {
        val target = txToCancel!!
        AlertDialog(
            onDismissRequest = { txToCancel = null },
            title = { Text("Confirm Sale Cancellation & Refund") },
            text = {
                Text("Are you sure you want to cancel invoice '${target.invoiceNumber}'? All sold item quantities will be automatically and atomically restored to store stock, and an audit log will be created.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrRefundSale(target.id, "Customer refund - cancelled by admin")
                        txToCancel = null
                        inspectingTx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Text("Confirm Refund & Restock", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToCancel = null }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryChangeType
import com.example.data.model.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryLogsScreen(viewModel: MainViewModel) {
    val logs by viewModel.allLogs.collectAsState()
    val products by viewModel.activeProducts.collectAsState()
    var isAdjustModalOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inventory Movement Audit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Immutable audit trail of sales deductions & restocking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { isAdjustModalOpen = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adjust Stock", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No inventory movements recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    val isPositive = log.quantityDelta > 0
                    val deltaColor = when {
                        log.changeType == InventoryChangeType.SALE -> RoseDanger
                        log.changeType == InventoryChangeType.RESTOCK -> EmeraldSecondary
                        else -> AmberWarning
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.productName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "SKU: ${log.sku} • By: ${log.performedByName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(deltaColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = deltaColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = if (log.quantityDelta > 0) "+${log.quantityDelta}" else "${log.quantityDelta}",
                                                color = deltaColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Balance: ${log.previousStock} → ${log.newStock} • Type: ${log.changeType.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (log.reason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Note: ${log.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IndigoLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAdjustModalOpen) {
        AdjustStockDialog(
            products = products,
            onDismiss = { isAdjustModalOpen = false },
            onConfirm = { prodId, newStock, type, reason ->
                viewModel.adjustStock(prodId, newStock, type, reason) {
                    isAdjustModalOpen = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustStockDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirm: (productId: String, newStock: Int, type: InventoryChangeType, reason: String) -> Unit
) {
    if (products.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Products") },
            text = { Text("Add products to your catalog before adjusting stock.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedProduct by remember { mutableStateOf(products.first()) }
    var expandedProduct by remember { mutableStateOf(false) }
    var newStockStr by remember { mutableStateOf(selectedProduct.stock.toString()) }
    var selectedType by remember { mutableStateOf(InventoryChangeType.RESTOCK) }
    var expandedType by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Product Stock", fontWeight = FontWeight.Bold) },
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

                // Product Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedProduct,
                    onExpandedChange = { expandedProduct = !expandedProduct }
                ) {
                    OutlinedTextField(
                        value = "${selectedProduct.name} (${selectedProduct.stock} in stock)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProduct) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProduct,
                        onDismissRequest = { expandedProduct = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} (SKU: ${prod.sku}, Stock: ${prod.stock})") },
                                onClick = {
                                    selectedProduct = prod
                                    newStockStr = prod.stock.toString()
                                    expandedProduct = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = newStockStr,
                    onValueChange = { newStockStr = it },
                    label = { Text("New Stock Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = when (selectedType) {
                            InventoryChangeType.RESTOCK -> "Restock / Shipment Arrival"
                            InventoryChangeType.ADJUSTMENT -> "Audit Count Correction"
                            InventoryChangeType.DAMAGED -> "Damaged / Expired Goods"
                            InventoryChangeType.RETURN -> "Customer Return"
                            else -> selectedType.name
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Adjustment Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        listOf(
                            InventoryChangeType.RESTOCK to "Restock / Shipment Arrival",
                            InventoryChangeType.ADJUSTMENT to "Audit Count Correction",
                            InventoryChangeType.DAMAGED to "Damaged / Expired Goods",
                            InventoryChangeType.RETURN to "Customer Return"
                        ).forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedType = type
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Note (Required)") },
                    placeholder = { Text("e.g. Supplier PO #9021 arrival") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stock = newStockStr.toIntOrNull()
                    if (stock == null || stock < 0) {
                        error = "Please enter a valid non-negative number."
                        return@Button
                    }
                    if (reason.isBlank()) {
                        error = "Please provide an adjustment reason for audit."
                        return@Button
                    }
                    onConfirm(selectedProduct.id, stock, selectedType, reason.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Text("Commit Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

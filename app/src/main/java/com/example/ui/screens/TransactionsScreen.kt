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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionRecord
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: MainViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    var search by remember { mutableStateOf("") }
    var inspectingTx by remember { mutableStateOf<TransactionRecord?>(null) }

    val filtered = transactions.filter {
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
        Text(
            text = "Sales & Invoices",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Complete transactional history and customer receipts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by invoice #, cashier, or payment method...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found.",
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

    if (inspectingTx != null) {
        ReceiptDialog(
            tx = inspectingTx!!,
            onDismiss = { inspectingTx = null }
        )
    }
}
